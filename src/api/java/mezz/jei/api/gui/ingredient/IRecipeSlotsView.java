package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import java.util.List;
import java.util.Optional;

/**
 * Represents all the drawn ingredients in slots that are part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * This view is meant as a source of information for recipe transfer, drawing, and tooltips.
 *
 * @since 9.3.0
 */
public interface IRecipeSlotsView {
	/**
	 * Get all slots for a recipe.
	 *
	 * @since 9.3.0
	 */
	List<IRecipeSlotView> getSlotViews();

	/**
	 * Get the list of slots for the given {@link RecipeIngredientRole} for a recipe.
	 *
	 * @since 9.3.0
	 */
	List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role);

	/**
	 * Get a recipe slot by its name set with {@link IRecipeSlotBuilder#setSlotName(String)}.
	 *
	 * @since 9.3.0
	 */
	Optional<IRecipeSlotView> findSlotByName(String slotName);
}
