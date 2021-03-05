package mezz.jei.load.registration;

import java.util.IdentityHashMap;
import java.util.Map;

import mezz.jei.api.ingredients.subtypes.IFluidSubtypeInterpreter;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.util.ErrorUtil;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubtypeRegistration implements ISubtypeRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<Item, ISubtypeInterpreter> interpreters = new IdentityHashMap<>();
	private final Map<Fluid, IFluidSubtypeInterpreter> fluidInterpreters = new IdentityHashMap<>();

	@Override
	public void useNbtForSubtypes(Item... items) {
		for (Item item : items) {
			registerSubtypeInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void useNbtForFluidSubtypes(Fluid... fluids) {
		for (Fluid fluid : fluids) {
			registerFluidSubtypeInterpreter(fluid, AllFluidNbt.INSTANCE);
		}
	}

	@Override
	public void registerSubtypeInterpreter(Item item, ISubtypeInterpreter interpreter) {
		ErrorUtil.checkNotNull(item, "item ");
		ErrorUtil.checkNotNull(interpreter, "interpreter");

		if (interpreters.containsKey(item)) {
			LOGGER.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		interpreters.put(item, interpreter);
	}

	@Override
	public void registerFluidSubtypeInterpreter(Fluid fluid, IFluidSubtypeInterpreter interpreter) {
		ErrorUtil.checkNotNull(fluid, "fluid ");
		ErrorUtil.checkNotNull(interpreter, "interpreter");

		if (fluidInterpreters.containsKey(fluid)) {
			LOGGER.error("An interpreter is already registered for this fluid: {}", fluid, new IllegalArgumentException());
			return;
		}

		fluidInterpreters.put(fluid, interpreter);
	}

	@Override
	public boolean hasSubtypeInterpreter(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);

		Item item = itemStack.getItem();
		return interpreters.containsKey(item);
	}

	@Override
	public boolean hasSubtypeInterpreter(FluidStack fluidStack) {
		ErrorUtil.checkNotEmpty(fluidStack);

		Fluid fluid = fluidStack.getFluid();
		return fluidInterpreters.containsKey(fluid);
	}

	public ImmutableMap<Item, ISubtypeInterpreter> getInterpreters() {
		return ImmutableMap.copyOf(interpreters);
	}

	public ImmutableMap<Fluid, IFluidSubtypeInterpreter> getFluidInterpreters() {
		return ImmutableMap.copyOf(fluidInterpreters);
	}

	private static class AllNbt implements ISubtypeInterpreter {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() {
		}

		@Override
		public String apply(ItemStack itemStack) {
			CompoundNBT nbtTagCompound = itemStack.getTag();
			if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
				return ISubtypeInterpreter.NONE;
			}
			return nbtTagCompound.toString();
		}
	}

	private static class AllFluidNbt implements IFluidSubtypeInterpreter {
		public static final AllFluidNbt INSTANCE = new AllFluidNbt();

		private AllFluidNbt() {
		}

		@Override
		public String apply(FluidStack fluidStack) {
			CompoundNBT nbtTagCompound = fluidStack.getTag();
			if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
				return IFluidSubtypeInterpreter.NONE;
			}
			return nbtTagCompound.toString();
		}
	}
}
