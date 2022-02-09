package mezz.jei.ingredients;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.ImmutableMap;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.load.registration.SubtypeRegistration;
import mezz.jei.util.ErrorUtil;
import net.minecraftforge.fluids.FluidStack;

public class SubtypeManager implements ISubtypeManager {
	private final ImmutableMap<Item, IIngredientSubtypeInterpreter<ItemStack>> itemInterpreters;
	private final ImmutableMap<Fluid, IIngredientSubtypeInterpreter<FluidStack>> fluidInterpreters;

	public SubtypeManager(SubtypeRegistration subtypeRegistration) {
		this.itemInterpreters = subtypeRegistration.getItemInterpreters();
		this.fluidInterpreters = subtypeRegistration.getFluidInterpreters();
	}

	@Nullable
	@Override
	public String getSubtypeInfo(ItemStack itemStack, UidContext context) {
		ErrorUtil.checkNotEmpty(itemStack);

		Item item = itemStack.getItem();
		IIngredientSubtypeInterpreter<ItemStack> subtypeInterpreter = itemInterpreters.get(item);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.apply(itemStack, context);
		}

		return null;
	}

	@Nullable
	@Override
	public String getSubtypeInfo(FluidStack fluidStack, UidContext context) {
		ErrorUtil.checkNotNull(fluidStack, "fluidStack");

		Fluid fluid = fluidStack.getFluid();
		IIngredientSubtypeInterpreter<FluidStack> subtypeInterpreter = fluidInterpreters.get(fluid);
		if (subtypeInterpreter != null) {
			return subtypeInterpreter.apply(fluidStack, context);
		}

		return null;
	}
}
