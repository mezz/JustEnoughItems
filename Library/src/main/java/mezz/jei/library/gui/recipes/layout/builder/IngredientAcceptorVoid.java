package mezz.jei.library.gui.recipes.layout.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class IngredientAcceptorVoid implements IIngredientAcceptor<IngredientAcceptorVoid> {
	public static final IngredientAcceptorVoid INSTANCE = new IngredientAcceptorVoid();

	private IngredientAcceptorVoid() {}

	@Override
	public IngredientAcceptorVoid addIngredientsUnsafe(List<?> ingredients) {
		return this;
	}

	@Override
	public <I> IngredientAcceptorVoid addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		return this;
	}

	@Override
	public <I> IngredientAcceptorVoid addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		return this;
	}

	@Override
	public IngredientAcceptorVoid addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		return this;
	}

	@Override
	public IngredientAcceptorVoid addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		return this;
	}

	@Override
	public IngredientAcceptorVoid addFluidStack(Fluid fluid) {
		return this;
	}

	@Override
	public IngredientAcceptorVoid addFluidStack(Fluid fluid, long amount) {
		return this;
	}

	@Override
	public IngredientAcceptorVoid addFluidStack(Fluid fluid, long amount, CompoundTag tag) {
		return this;
	}
}
