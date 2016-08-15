package mezz.jei;

import javax.annotation.Nonnull;
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
	public void useNbtForSubtypes(@Nonnull Item... items) {
		for (Item item : items) {
			registerNbtInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void registerNbtInterpreter(@Nullable Item item, @Nullable ISubtypeInterpreter interpreter) {
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
				FluidStack contents = tankProperties.getContents();
				if (contents != null) {
					Fluid fluid = contents.getFluid();
					if (fluid != null) {
						info.append(fluid.getName()).append(";");
					}
				}
			}
			if (info.length() > 0) {
				return info.toString();
			}
		}

		return null;

	}

	private static class AllNbt implements ISubtypeInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() { }

		@Nullable
		@Override
		public String getSubtypeInfo(@Nonnull ItemStack itemStack) {
			NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
			if (nbtTagCompound == null || nbtTagCompound.hasNoTags()) {
				return null;
			}
			return nbtTagCompound.toString();
		}
	}
}
