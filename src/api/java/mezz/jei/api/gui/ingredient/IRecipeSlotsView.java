package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import java.util.List;

/**
 * Represents all the drawn ingredients in slots that are part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * This view is meant to replace {@link IRecipeLayout} and {@link IGuiIngredientGroup} for recipe transfer.
 *
 * @since JEI 9.3.0
 */
public interface IRecipeSlotsView {
	/**
	 * Get the list of slots that have ingredients in them for a recipe.
	 * Slot numbers reference real slots in the gui.
	 *
	 * @since JEI 9.3.0
	 */
	List<IRecipeSlotView> getSlotViews();

	/**
	 * Get the list of slots that have ingredients in them for a recipe,
	 * filtered by a {@link RecipeIngredientRole} and {@link IIngredientType}.
	 *
	 * @since JEI 9.3.0
	 */
	List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role, IIngredientType<?> ingredientType);
}
