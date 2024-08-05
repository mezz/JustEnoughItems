package mezz.jei.library.gui.recipes.layout.builder;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.IngredientAcceptor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class InvisibleRecipeLayoutSlotSource implements IIngredientAcceptor<InvisibleRecipeLayoutSlotSource> {
	private final IngredientAcceptor ingredients;
	private final RecipeIngredientRole role;

	public InvisibleRecipeLayoutSlotSource(IIngredientManager ingredientManager, RecipeIngredientRole role) {
		this.ingredients = new IngredientAcceptor(ingredientManager);
		this.role = role;
	}

	@Override
	public InvisibleRecipeLayoutSlotSource addIngredientsUnsafe(List<?> ingredients) {
		this.ingredients.addIngredientsUnsafe(ingredients);
		return this;
	}

	@Override
	public <I> InvisibleRecipeLayoutSlotSource addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> InvisibleRecipeLayoutSlotSource addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		this.ingredients.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public InvisibleRecipeLayoutSlotSource addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		this.ingredients.addTypedIngredients(ingredients);
		return this;
	}

	@Override
	public InvisibleRecipeLayoutSlotSource addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		this.ingredients.addOptionalTypedIngredients(ingredients);
		return this;
	}

	@Override
	public InvisibleRecipeLayoutSlotSource addFluidStack(Fluid fluid, long amount) {
		this.ingredients.addFluidStack(fluid, amount);
		return this;
	}

	@Override
	public InvisibleRecipeLayoutSlotSource addFluidStack(Fluid fluid, long amount, DataComponentPatch componentPatch) {
		this.ingredients.addFluidStack(fluid, amount, componentPatch);
		return this;
	}

	public RecipeSlotIngredients getRecipeSlotIngredients() {
		return new RecipeSlotIngredients(
			this.role,
			this.ingredients.getAllIngredients(),
			this.ingredients.getIngredientTypes()
		);
	}
}
