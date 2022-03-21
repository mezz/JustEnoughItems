package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents one drawn ingredient that is part of a recipe.
 * One recipe slot can contain multiple ingredients, displayed one after the other over time.
 *
 * These ingredients may be different types, for example {@link VanillaTypes#ITEM} and {@link VanillaTypes#FLUID}
 * can be displayed in one recipe slot in rotation.
 *
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * @since 9.3.0
 */
public interface IRecipeSlotView {
	/**
	 * All ingredient variations of the given type that can be shown.
	 *
	 * @see #getAllIngredients() to get ingredients of every type together.
	 *
	 * @since 9.3.0
	 */
	<T> Stream<T> getIngredients(IIngredientType<T> ingredientType);

	/**
	 * All ingredient variations that can be shown.
	 *
	 * @see #getIngredients(IIngredientType) to limit to one type of ingredient.
	 *
	 * @since 9.3.0
	 */
	Stream<ITypedIngredient<?>> getAllIngredients();

	/**
	 * @return true if there are no ingredients in this recipe slot.
	 *
	 * @since 9.3.0
	 */
	boolean isEmpty();

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 * If nothing of this type is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since 9.3.0
	 */
	<T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType);

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 * If nothing is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since 9.3.0
	 */
	Optional<ITypedIngredient<?>> getDisplayedIngredient();

	/**
	 * The slot's name if one was set by {@link IRecipeSlotBuilder#setSlotName(String)}
	 *
	 * @since 9.3.0
	 */
	Optional<String> getSlotName();

	/**
	 * Returns the type of focus that matches this ingredient.
	 *
	 * @since 9.3.0
	 */
	RecipeIngredientRole getRole();

	/**
	 * Draws a highlight on background of this ingredient.
	 * This is used by recipe transfer errors to turn missing ingredient backgrounds to red, but can be used for other purposes.
	 *
	 * @see IRecipeTransferHandlerHelper#createUserErrorForMissingSlots(Component, Collection).
	 *
	 * @since 9.3.0
	 */
	void drawHighlight(PoseStack stack, int color);
}
