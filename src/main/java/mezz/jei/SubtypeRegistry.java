package mezz.jei;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.util.Log;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class SubtypeRegistry implements ISubtypeRegistry {
	private final Map<Item, ISubtypeInterpreter> interpreters = new HashMap<Item, ISubtypeInterpreter>();

	@Override
	public void useNbtForSubtypes(Item... items) {
		for (Item item : items) {
			registerSubtypeInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void registerNbtInterpreter(@Nullable Item item, @Nullable ISubtypeInterpreter interpreter) {
		registerSubtypeInterpreter(item, interpreter);
	}

	@Override
	public void registerSubtypeInterpreter(@Nullable Item item, @Nullable ISubtypeInterpreter interpreter) {
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return;
		}
		if (interpreter == null) {
			Log.error("Null interpreter", new NullPointerException());
			return;
		}

		if (interpreters.containsKey(item)) {
			Log.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		interpreters.put(item, interpreter);
	}

	@Nullable
	@Override
	public String getSubtypeInfo(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			Log.error("Null itemStack", new NullPointerException());
			return null;
		}

		Item item = itemStack.getItem();
		if (item == null) {
			Log.error("Null item", new NullPointerException());
			return null;
		}

		ISubtypeInterpreter nbtInterpreter = interpreters.get(item);
		if (nbtInterpreter != null) {
			return nbtInterpreter.getSubtypeInfo(itemStack);
		}

		if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
			IFluidHandler capability = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
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

	private static class AllNbt implements ISubtypeInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() {
		}

		@Nullable
		@Override
		public String getSubtypeInfo(ItemStack itemStack) {
			NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
			if (nbtTagCompound == null || nbtTagCompound.hasNoTags()) {
				return null;
			}
			return nbtTagCompound.toString();
		}
	}
}
