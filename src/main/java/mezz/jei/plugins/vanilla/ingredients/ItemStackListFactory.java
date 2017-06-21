package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.config.Config;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class ItemStackListFactory {
	private final ISubtypeRegistry subtypeRegistry;
	private final Multiset<Item> subtypeCount = HashMultiset.create();

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
				subtypeCount.add(itemStack.getItem());
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					Log.get().error("Found an empty itemStack from creative tab: {}", creativeTab);
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

		NonNullList<ItemStack> items = stackHelper.getSubtypes(item, 1);
		subtypeCount.setCount(item, items.size());
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
		Item item = stack.getItem();
		if (subtypeCount.count(item) >= Config.getMaxSubtypes()) {
			return;
		}

		String itemKey = null;

		try {
			addFallbackSubtypeInterpreter(stack);
			itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
		} catch (RuntimeException | LinkageError e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			Log.get().error("Couldn't get unique name for itemStack {}", stackInfo, e);
		}

		if (itemKey != null) {
			if (itemNameSet.contains(itemKey)) {
				return;
			}
			itemNameSet.add(itemKey);
			itemList.add(stack);
		}
	}

	private void addFallbackSubtypeInterpreter(ItemStack itemStack) {
		if (!this.subtypeRegistry.hasSubtypeInterpreter(itemStack)) {
			if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
				this.subtypeRegistry.registerSubtypeInterpreter(itemStack.getItem(), FluidSubtypeInterpreter.INSTANCE);
			}
		}
	}

	private static class FluidSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
		public static final FluidSubtypeInterpreter INSTANCE = new FluidSubtypeInterpreter();

		private FluidSubtypeInterpreter() {

		}

		@Nullable
		@Override
		public String getSubtypeInfo(ItemStack itemStack) {
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
			return null;
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
