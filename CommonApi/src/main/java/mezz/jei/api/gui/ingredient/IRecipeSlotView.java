package mezz.jei.api.gui.ingredient;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents one drawn ingredient that is part of a recipe.
 * One recipe slot can contain multiple ingredients, displayed one after the other over time.
 *
 * These ingredients may be different types, for example {@link VanillaTypes#ITEM_STACK} and another type
 * can be displayed in one recipe slot in rotation.
 *
 * Useful for implementing {@link IRecipeTransferHandler} and some other advanced cases.
 *
 * @since 9.3.0
 */
@ApiStatus.NonExtendable
public interface IRecipeSlotView {
	/**
	 * All ingredient variations that can be shown, ignoring focus and visibility.
	 *
	 * @see #getItemStacks() to limit to only ItemStack ingredients.
	 * @see #getIngredients(IIngredientType) to limit to one type of ingredient.
	 *
	 * @since 9.3.0
	 */
	Stream<ITypedIngredient<?>> getAllIngredients();

	/**
	 * All ingredients, ignoring focus and visibility
	 * null ingredients represent a "blank" drawn ingredient in the rotation.
	 *
	 * @since 19.19.5
	 */
	@Unmodifiable
	List<@Nullable ITypedIngredient<?>> getAllIngredientsList();

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 * If nothing is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since 9.3.0
	 */
	Optional<ITypedIngredient<?>> getDisplayedIngredient();

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
	 * @see IRecipeTransferHandlerHelper#createUserErrorForMissingSlots(Component, Collection)
	 *
	 * @since 9.3.0
	 */
	void drawHighlight(GuiGraphics guiGraphics, int color);

	/**
	 * All ingredient variations of the given type that can be shown.
	 *
	 * @see #getItemStacks() to get only ItemStacks
	 * @see #getAllIngredients() to get ingredients of every type together.
	 *
	 * @since 9.3.0
	 */
	default <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return getAllIngredients()
			.map(i -> i.getIngredient(ingredientType))
			.flatMap(Optional::stream);
	}

	/**
	 * All ingredient variations of the given type that can be shown.
	 *
	 * @see #getIngredients(IIngredientType) to get different types of ingredients.
	 * @see #getAllIngredients() to get ingredients of every type together.
	 *
	 * @since 11.1.1
	 */
	default Stream<ItemStack> getItemStacks() {
		return getIngredients(VanillaTypes.ITEM_STACK);
	}

	/**
	 * @return true if there are no ingredients in this recipe slot.
	 *
	 * @since 9.3.0
	 */
	default boolean isEmpty() {
		return getAllIngredients().findAny().isEmpty();
	}

	/**
	 * The ItemStack variation that is shown at this moment.
	 *
	 * For ingredients that rotate through several values, this will change over time.
	 * If no ItemStack is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since 11.1.1
	 */
	default Optional<ItemStack> getDisplayedItemStack() {
		return getDisplayedIngredient(VanillaTypes.ITEM_STACK);
	}

	/**
	 * The ingredient variation that is shown at this moment.
	 * For ingredients that rotate through several values, this will change over time.
	 * If nothing of this type is currently shown, this will return {@link Optional#empty()}.
	 *
	 * @since 9.3.0
	 */
	default <T> Optional<T> getDisplayedIngredient(IIngredientType<T> ingredientType) {
		return getDisplayedIngredient()
			.flatMap(i -> i.getIngredient(ingredientType));
	}

	/**
	 * The slot's name if one was set by {@link IRecipeSlotBuilder#setSlotName(String)}
	 *
	 * @since 9.3.0
	 */
	Optional<String> getSlotName();
}
