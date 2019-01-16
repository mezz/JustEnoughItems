package mezz.jei.api.gui;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * Represents one drawn ingredient that is part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 * Get these from {@link IGuiIngredientGroup#getGuiIngredients()}.
 */
public interface IGuiIngredient<T> {
	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 *
	 * @since JEI 3.11.0
	 */
	@Nullable
	T getDisplayedIngredient();

	/**
	 * All ingredient variations that can be shown.
	 * For ingredients that rotate through several values, this will have them all even if a focus is set.
	 * This list can contain null values.
	 */
	List<T> getAllIngredients();

	/**
	 * Returns true if this ingredient is an input for the recipe, otherwise it is an output.
	 */
	boolean isInput();

	/**
	 * Draws a highlight on background of this ingredient.
	 * This is used by recipe transfer errors to turn missing ingredient backgrounds to red, but can be used for other purposes.
	 *
	 * @see IRecipeTransferHandlerHelper#createUserErrorForSlots(String, Collection).
	 */
	void drawHighlight(Minecraft minecraft, Color color, int xOffset, int yOffset);
}
