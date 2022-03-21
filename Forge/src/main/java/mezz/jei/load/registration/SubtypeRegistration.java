package mezz.jei.load.registration;

import java.util.IdentityHashMap;
import java.util.Map;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.util.ErrorUtil;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubtypeRegistration implements ISubtypeRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Map<Item, IIngredientSubtypeInterpreter<ItemStack>> itemInterpreters = new IdentityHashMap<>();
	private final Map<Fluid, IIngredientSubtypeInterpreter<FluidStack>> fluidInterpreters = new IdentityHashMap<>();

	@Override
	public void useNbtForSubtypes(Item... items) {
		for (Item item : items) {
			registerSubtypeInterpreter(item, AllNbt.INSTANCE);
		}
	}

	@Override
	public void useNbtForSubtypes(Fluid... fluids) {
		for (Fluid fluid : fluids) {
			registerSubtypeInterpreter(fluid, AllFluidNbt.INSTANCE);
		}
	}

	@Override
	public void registerSubtypeInterpreter(Item item, IIngredientSubtypeInterpreter<ItemStack> interpreter) {
		ErrorUtil.checkNotNull(item, "item ");
		ErrorUtil.checkNotNull(interpreter, "interpreter");

		if (itemInterpreters.containsKey(item)) {
			LOGGER.error("An interpreter is already registered for this item: {}", item, new IllegalArgumentException());
			return;
		}

		itemInterpreters.put(item, interpreter);
	}

	@Override
	public void registerSubtypeInterpreter(Fluid fluid, IIngredientSubtypeInterpreter<FluidStack> interpreter) {
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
		return itemInterpreters.containsKey(item);
	}

	@Override
	public boolean hasSubtypeInterpreter(FluidStack fluidStack) {
		ErrorUtil.checkNotNull(fluidStack, "fluid ");

		Fluid fluid = fluidStack.getFluid();
		return fluidInterpreters.containsKey(fluid);
	}

	public ImmutableMap<Item, IIngredientSubtypeInterpreter<ItemStack>> getItemInterpreters() {
		return ImmutableMap.copyOf(itemInterpreters);
	}

	public ImmutableMap<Fluid, IIngredientSubtypeInterpreter<FluidStack>> getFluidInterpreters() {
		return ImmutableMap.copyOf(fluidInterpreters);
	}

	private static class AllNbt implements IIngredientSubtypeInterpreter<ItemStack> {
		public static final AllNbt INSTANCE = new AllNbt();

		private AllNbt() {
		}

		@Override
		public String apply(ItemStack itemStack, UidContext context) {
			CompoundTag nbtTagCompound = itemStack.getTag();
			if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
				return IIngredientSubtypeInterpreter.NONE;
			}
			return nbtTagCompound.toString();
		}
	}

	private static class AllFluidNbt implements IIngredientSubtypeInterpreter<FluidStack> {
		public static final AllFluidNbt INSTANCE = new AllFluidNbt();

		private AllFluidNbt() {
		}

		@Override
		public String apply(FluidStack fluidStack, UidContext context) {
			CompoundTag nbtTagCompound = fluidStack.getTag();
			if (nbtTagCompound == null || nbtTagCompound.isEmpty()) {
				return IIngredientSubtypeInterpreter.NONE;
			}
			return nbtTagCompound.toString();
		}
	}
}
