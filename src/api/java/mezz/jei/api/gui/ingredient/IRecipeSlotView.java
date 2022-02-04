package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeSlotId;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Represents one drawn ingredient that is part of a recipe.
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * @since JEI 9.3.0
 */
public interface IRecipeSlotView {
	/**
	 * All ingredient variations of the given type that can be shown.
	 *
	 * @see #getAllIngredients() to get ingredients of every type together.
	 *
	 * @since JEI 9.3.0
	 */
	<T> Stream<T> getIngredients(IIngredientType<T> ingredientType);

	/**
	 * All ingredient variations that can be shown.
	 * This list can contain multiple types of ingredient.
	 *
	 * @see #getIngredients(IIngredientType) to limit to one type of ingredient.
	 *
	 * @since JEI 9.3.0
	 */
	Stream<ITypedIngredient<?>> getAllIngredients();

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 * If nothing of this type is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since JEI 9.3.0
	 */
	<T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType);

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 * If nothing is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since JEI 9.3.0
	 */
	Optional<ITypedIngredient<?>> getDisplayedIngredient();

	/**
	 * For recipe transfer, returns the ({@link Slot#index} of this ingredient if it has one.
	 *
	 * @since JEI 9.3.0
	 */
	OptionalInt getContainerSlotIndex();

	/**
	 * The unique {@link IRecipeSlotId} of this slot.
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotId getSlotId();

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
