package mezz.jei.api.ingredients;

import java.util.List;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * This holds a recipe's inputs and outputs in a standardized format.
 *
 * It is filled out by {@link IRecipeCategory#setIngredients(Object, IMutableIngredients)}, and then used by JEI to figure out
 * what items are in the recipe, for lookups.
 * It is also passed back to {@link IRecipeCategory#setRecipe(IRecipeLayout, Object, IIngredients)}
 * where it can be used to set ingredient groups in the recipe layout with {@link IGuiIngredientGroup#set(IIngredients)}
 */
public interface IIngredients {
	/**
	 * Get all the inputs that have been set for the ingredientClass.
	 * Each list element represents one slot. The inner list represents the ingredient(s) in the slot.
	 */
	<T> List<List<T>> getInputs(IIngredientType<T> ingredientType);

	/**
	 * Get all the outputs that have been set for the ingredientClass.
	 * Each list element represents one slot.
	 */
	<T> List<List<T>> getOutputs(IIngredientType<T> ingredientType);
}
