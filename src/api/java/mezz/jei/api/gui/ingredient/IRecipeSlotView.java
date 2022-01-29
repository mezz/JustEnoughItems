package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents one drawn ingredient that is part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * @since JEI 9.3.0
 */
public interface IRecipeSlotView {
	/**
	 * All ingredient variations that can be shown.
	 * This list can contain multiple types of ingredient.
	 *
	 * @see #getAllIngredients(IIngredientType) to limit to one type of ingredient.
	 *
	 * @since JEI 9.3.0
	 */
	List<?> getAllIngredients();

	/**
	 * All ingredient variations of the given type that can be shown.
	 *
	 * @since JEI 9.3.0
	 */
	<T> Stream<T> getAllIngredients(IIngredientType<T> ingredientType);

	/**
	 * Returns the ({@link Slot#index} of this ingredient.
	 *
	 * @since JEI 9.3.0
	 */
	int getSlotIndex();

	/**
	 * Returns the type of focus that matches this ingredient.
	 *
	 * @since JEI 9.3.0
	 */
	RecipeIngredientRole getRole();

	/**
	 * Draws a highlight on background of this ingredient.
	 * This is used by recipe transfer errors to turn missing ingredient backgrounds to red, but can be used for other purposes.
	 *
	 * @see IRecipeTransferHandlerHelper#createUserErrorForMissingSlots(Component, Collection).
	 *
	 * @since JEI 9.3.0
	 */
	void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset);
}
