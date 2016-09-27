package mezz.jei.api.ingredients;

import java.util.List;

import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * This holds a recipe's inputs and outputs in a standardized format.
 * <p>
 * It is filled out by {@link IRecipeWrapper#getIngredients(IIngredients)}, and then used by JEI to figure out
 * what items are in the recipe, for lookups.
 * It is also passed back to {@link IRecipeCategory#setRecipe(IRecipeLayout, IRecipeWrapper, IIngredients)}
 * where it can be used to set ingredient groups in the recipe layout with {@link IGuiIngredientGroup#set(IIngredients)}
 *
 * @since JEI 3.11.0
 */
public interface IIngredients {
	/**
	 * Sets a single recipe input. For recipes with only one input slot.
	 *
	 * @param ingredientClass The class of ingredient: ItemStack.class, FluidStack.class, etc.
	 * @param input           The list of ingredients representing each input slot.
	 */
	<T> void setInput(Class<T> ingredientClass, T input);

	/**
	 * Sets the recipe's inputs. Each list element represents one slot.
	 *
	 * @param ingredientClass The class of ingredient: ItemStack.class, FluidStack.class, etc.
	 * @param input           The list of ingredients representing each input slot.
	 */
	<T> void setInputs(Class<T> ingredientClass, List<T> input);

	/**
	 * Sets the recipe's inputs. Each input list represents one slot.
	 * Accepts multiple ingredients per slot.
	 *
	 * @param ingredientClass The class of ingredient: ItemStack.class, FluidStack.class, etc.
	 * @param inputs          The outer list represents the slot, the inner list is a rotating list of ingredients in that slot.
	 */
	<T> void setInputLists(Class<T> ingredientClass, List<List<T>> inputs);

	/**
	 * Sets a single recipe output.
	 *
	 * @param ingredientClass The class of ingredient: ItemStack.class, FluidStack.class, etc.
	 * @param output          The single ingredient representing the recipe output.
	 */
	<T> void setOutput(Class<T> ingredientClass, T output);

	/**
	 * Sets multiple recipe outputs. Each list element represents one slot.
	 * Note that recipes can't have multiple outputs for one slot.
	 *
	 * @param ingredientClass The class of ingredient: ItemStack.class, FluidStack.class, etc.
	 * @param outputs         The list of ingredients representing each output slot.
	 */
	<T> void setOutputs(Class<T> ingredientClass, List<T> outputs);

	/**
	 * Get all the inputs that have been set for the ingredientClass.
	 * Each list element represents one slot. The inner list represents the ingredient(s) in the slot.
	 */
	<T> List<List<T>> getInputs(Class<T> ingredientClass);

	/**
	 * Get all the outputs that have been set for the ingredientClass.
	 * Each list element represents one slot.
	 */
	<T> List<T> getOutputs(Class<T> ingredientClass);
}
