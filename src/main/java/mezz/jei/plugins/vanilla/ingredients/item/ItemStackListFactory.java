package mezz.jei.plugins.vanilla.ingredients.item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public final class ItemStackListFactory {
	private final ISubtypeRegistry subtypeRegistry;

	public ItemStackListFactory(ISubtypeRegistry subtypeRegistry) {
		this.subtypeRegistry = subtypeRegistry;
	}

	public List<ItemStack> create(StackHelper stackHelper) {
		final List<ItemStack> itemList = new ArrayList<>();
		final Set<String> itemNameSet = new HashSet<>();

		for (CreativeTabs creativeTab : CreativeTabs.CREATIVE_TAB_ARRAY) {
			if (creativeTab == CreativeTabs.HOTBAR) {
				continue;
			}
			NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
			try {
				creativeTab.displayAllRelevantItems(creativeTabItemStacks);
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Creative tab crashed while getting items. Some items from this tab will be missing from the item list. {}", creativeTab, e);
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					Log.get().error("Found an empty itemStack from creative tab: {}", creativeTab);
				} else if (itemStack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
					String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
					Log.get().error("Found an itemStack with wildcard metadata from creative tab: {}. {}", creativeTab, itemStackInfo);
				} else {
					addItemStack(stackHelper, itemStack, itemList, itemNameSet);
				}
			}
		}

		for (Block block : ForgeRegistries.BLOCKS) {
			addBlockAndSubBlocks(stackHelper, block, itemList, itemNameSet);
		}

		for (Item item : ForgeRegistries.ITEMS) {
			addItemAndSubItems(stackHelper, item, itemList, itemNameSet);
		}

		return itemList;
	}

	private void addItemAndSubItems(StackHelper stackHelper, @Nullable Item item, List<ItemStack> itemList, Set<String> itemNameSet) {
		if (item == null || item == Items.AIR) {
			return;
		}
		NonNullList<ItemStack> items = NonNullList.create();
		stackHelper.addSubtypesToList(items, item);
		for (ItemStack stack : items) {
			addItemStack(stackHelper, stack, itemList, itemNameSet);
		}
	}

	private void addBlockAndSubBlocks(StackHelper stackHelper, @Nullable Block block, List<ItemStack> itemList, Set<String> itemNameSet) {
		if (block == null) {
			return;
		}

		Item item = Item.getItemFromBlock(block);
		if (item == Items.AIR) {
			return;
		}

		for (CreativeTabs itemTab : item.getCreativeTabs()) {
			NonNullList<ItemStack> subBlocks = NonNullList.create();
			try {
				block.getSubBlocks(itemTab, subBlocks);
			} catch (RuntimeException | LinkageError e) {
				String itemStackInfo = ErrorUtil.getItemStackInfo(new ItemStack(item));
				Log.get().error("Failed to getSubBlocks {}", itemStackInfo, e);
			}

			for (ItemStack subBlock : subBlocks) {
				if (subBlock == null) {
					Log.get().error("Found null subBlock of {}", block);
				} else if (subBlock.isEmpty()) {
					Log.get().error("Found empty subBlock of {}", block);
				} else {
					addItemStack(stackHelper, subBlock, itemList, itemNameSet);
				}
			}
		}
	}

	private void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
		final String itemKey;

		try {
			addFallbackSubtypeInterpreter(stack);
			itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
		} catch (RuntimeException | LinkageError e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			Log.get().error("Couldn't get unique name for itemStack {}", stackInfo, e);
			return;
		}

		if (!itemNameSet.contains(itemKey)) {
			itemNameSet.add(itemKey);
			itemList.add(stack);
		}
	}

	private void addFallbackSubtypeInterpreter(ItemStack itemStack) {
		if (!this.subtypeRegistry.hasSubtypeInterpreter(itemStack)) {
			try {
				String info = FluidSubtypeInterpreter.INSTANCE.apply(itemStack);
				if (!ISubtypeRegistry.ISubtypeInterpreter.NONE.equals(info)) {
					this.subtypeRegistry.registerSubtypeInterpreter(itemStack.getItem(), FluidSubtypeInterpreter.INSTANCE);
				}
			} catch (RuntimeException | LinkageError e) {
				String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
				Log.get().error("Failed to apply FluidSubtypeInterpreter to ItemStack: {}", itemStackInfo, e);
			}
		}
	}

	private static class FluidSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
		public static final FluidSubtypeInterpreter INSTANCE = new FluidSubtypeInterpreter();

		private FluidSubtypeInterpreter() {

		}

		@Override
		public String apply(ItemStack itemStack) {
			if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
				IFluidHandler capability = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
				if (capability != null) {
					IFluidTankProperties[] tankPropertiesList = capability.getTankProperties();
					StringBuilder info = new StringBuilder();
					for (IFluidTankProperties tankProperties : tankPropertiesList) {
						String contentsName = getContentsName(tankProperties);
						if (contentsName != null) {
							info.append(contentsName).append(";");
						} else {
							info.append("empty").append(";");
						}
					}
					if (info.length() > 0) {
						if (itemStack.getHasSubtypes()) {
							info.append("m=").append(itemStack.getMetadata());
						}
						return info.toString();
					}
				}
			}
			return ISubtypeRegistry.ISubtypeInterpreter.NONE;
		}

		@Nullable
		private static String getContentsName(IFluidTankProperties fluidTankProperties) {
			FluidStack contents = fluidTankProperties.getContents();
			if (contents != null) {
				Fluid fluid = contents.getFluid();
				if (fluid != null) {
					return fluid.getName();
				}
			}
			return null;
		}
	}
}
