package mezz.jei.gui.recipes.builder;

import com.google.common.base.Preconditions;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.ingredients.IngredientsForTypeMap;
import mezz.jei.util.ErrorUtil;

import java.util.List;

public class InvisibleRecipeLayoutSlotSource implements IRecipeLayoutSlotSource, IIngredientAcceptor<InvisibleRecipeLayoutSlotSource> {
	private final IngredientsForTypeMap ingredients = new IngredientsForTypeMap();
	private final RecipeIngredientRole role;

	public InvisibleRecipeLayoutSlotSource(RecipeIngredientRole role) {
		this.role = role;
	}

	@Override
	public <I> InvisibleRecipeLayoutSlotSource addIngredients(IIngredientType<I> ingredientType, List<I> ingredients) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		Preconditions.checkNotNull(ingredients, "ingredients");
		this.ingredients.addIngredients(ingredientType, ingredients);
		return this;
	}

	@Override
	public <I> InvisibleRecipeLayoutSlotSource addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredients, "ingredient");
		this.ingredients.addIngredient(ingredientType, ingredient);
		return this;
	}

	@Override
	public RecipeIngredientRole getRole() {
		return this.role;
	}

	@Override
	public IngredientsForTypeMap getIngredientsForTypeMap() {
		return this.ingredients;
	}

	@Override
	public <R> void setRecipeLayout(RecipeLayout<R> recipeLayout) {
		// invisible, don't set the layout
	}
}
