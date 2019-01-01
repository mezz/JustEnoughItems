package mezz.jei.plugins.vanilla.ingredients.item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.startup.StackHelper;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ItemStackListFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ISubtypeRegistry subtypeRegistry;

	public ItemStackListFactory(ISubtypeRegistry subtypeRegistry) {
		this.subtypeRegistry = subtypeRegistry;
	}

	public List<ItemStack> create(StackHelper stackHelper) {
		final List<ItemStack> itemList = new ArrayList<>();
		final Set<String> itemNameSet = new HashSet<>();

		for (ItemGroup itemGroup : ItemGroup.GROUPS) {
			if (itemGroup == ItemGroup.HOTBAR || itemGroup == ItemGroup.INVENTORY) {
				continue;
			}
			NonNullList<ItemStack> creativeTabItemStacks = NonNullList.create();
			try {
				itemGroup.fill(creativeTabItemStacks);
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error("Item Group crashed while getting items." +
					"Some items from this group will be missing from the ingredient list. {}", itemGroup, e);
			}
			for (ItemStack itemStack : creativeTabItemStacks) {
				if (itemStack.isEmpty()) {
					LOGGER.error("Found an empty itemStack from creative tab: {}", itemGroup);
				} else {
					addItemStack(stackHelper, itemStack, itemList, itemNameSet);
				}
			}
		}
		return itemList;
	}

	private void addItemStack(StackHelper stackHelper, ItemStack stack, List<ItemStack> itemList, Set<String> itemNameSet) {
		final String itemKey;

		try {
			addFallbackSubtypeInterpreter(stack);
			itemKey = stackHelper.getUniqueIdentifierForStack(stack, StackHelper.UidMode.FULL);
		} catch (RuntimeException | LinkageError e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			LOGGER.error("Couldn't get unique name for itemStack {}", stackInfo, e);
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
				LOGGER.error("Failed to apply FluidSubtypeInterpreter to ItemStack: {}", itemStackInfo, e);
			}
		}
	}

	private static class FluidSubtypeInterpreter implements ISubtypeRegistry.ISubtypeInterpreter {
		public static final FluidSubtypeInterpreter INSTANCE = new FluidSubtypeInterpreter();

		private FluidSubtypeInterpreter() {

		}

		@Override
		public String apply(ItemStack itemStack) {
			return itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(capability -> {
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
					return info.toString();
				}
				return ISubtypeRegistry.ISubtypeInterpreter.NONE;
			}).orElse(ISubtypeRegistry.ISubtypeInterpreter.NONE);
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
