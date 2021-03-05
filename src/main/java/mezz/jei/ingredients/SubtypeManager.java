package mezz.jei.ingredients;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.subtypes.IFluidSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.util.ErrorUtil;
import net.minecraftforge.fluids.FluidStack;

public class SubtypeManager implements ISubtypeManager {
	private final ImmutableMap<Item, ISubtypeInterpreter> interpreters;
	private final ImmutableMap<Fluid, IFluidSubtypeInterpreter> fluidInterpreters;

	public SubtypeManager(SubtypeRegistration subtypeRegistration) {
		this.interpreters = subtypeRegistration.getInterpreters();
		this.fluidInterpreters = subtypeRegistration.getFluidInterpreters();
	}

	@Nullable
	@Override
	public String getSubtypeInfo(ItemStack itemStack) {
		ErrorUtil.checkNotEmpty(itemStack);

		Item item = itemStack.getItem();
		ISubtypeInterpreter subtypeInterpreter = interpreters.get(item);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.apply(itemStack);
		}

		return null;
	}

	@Nullable
	@Override
	public String getSubtypeInfo(ItemStack itemStack, UidContext context) {
		ErrorUtil.checkNotEmpty(itemStack);

		Item item = itemStack.getItem();
		ISubtypeInterpreter subtypeInterpreter = interpreters.get(item);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.apply(itemStack, context);
		}

		return null;
	}

	@Nullable
	@Override
	public String getSubtypeInfo(FluidStack fluidStack, UidContext context) {
		ErrorUtil.checkNotEmpty(fluidStack);

		Fluid fluid = fluidStack.getFluid();
		IFluidSubtypeInterpreter subtypeInterpreter = fluidInterpreters.get(fluid);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.apply(fluidStack, context);
		}

		return null;
	}
}
