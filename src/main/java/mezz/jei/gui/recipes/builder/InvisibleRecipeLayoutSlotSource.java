package mezz.jei.gui.recipes.builder;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientAcceptor;
import mezz.jei.ingredients.RegisteredIngredients;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class InvisibleRecipeLayoutSlotSource implements IRecipeLayoutSlotSource, IIngredientAcceptor<InvisibleRecipeLayoutSlotSource> {
	private final IngredientAcceptor ingredients;
	private final RecipeIngredientRole role;

	public InvisibleRecipeLayoutSlotSource(RegisteredIngredients registeredIngredients, RecipeIngredientRole role) {
		this.ingredients = new IngredientAcceptor(registeredIngredients);
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
	public RecipeIngredientRole getRole() {
		return this.role;
	}

	@Override
	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout, IntSet focusedIndexes) {
		// invisible, don't set the layout
	}

	@Override
	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return this.ingredients.getIngredients(ingredientType);
	}

	@Override
	public IntSet getMatches(IFocusGroup focuses) {
		return this.ingredients.getMatches(focuses, role);
	}

	@Override
	public int getIngredientCount() {
		return this.ingredients.getAllIngredients().size();
	}

	@Override
	public Stream<IIngredientType<?>> getIngredientTypes() {
		return this.ingredients.getIngredientTypes();
	}
}
