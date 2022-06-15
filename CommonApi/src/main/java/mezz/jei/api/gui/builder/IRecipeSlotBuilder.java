package mezz.jei.api.gui.builder;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;

/**
 * Allows setting properties of a slot on a {@link IRecipeLayoutBuilder}.
 * Implements {@link IIngredientAcceptor} to add ingredients to the slot.
 *
 * @see IIngredientAcceptor for methods to add ingredients to this builder.
 *
 * @since 9.3.0
 */
public interface IRecipeSlotBuilder extends IIngredientAcceptor<IRecipeSlotBuilder> {
	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 *
	 * @see IRecipeSlotTooltipCallback
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);

	/**
	 * Give the slot a unique name, for looking it up later by using
	 * {@link IRecipeSlotsView#findSlotByName(String)}
	 * in {@link IRecipeCategory#draw}
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setSlotName(String slotName);

	/**
	 * Set a custom background to draw behind the slot's ingredients.
	 *
	 * @param xOffset The amount to offset the background from the ingredient in the X direction.
	 *                May be negative, the background can be drawn larger than the ingredient.
	 * @param yOffset The amount to offset the background from the ingredient in the Y direction.
	 *                May be negative, the background can be drawn larger than the ingredient.
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset);

	/**
	 * Set an overlay to draw on top of the slot's ingredient.
	 *
	 * @param xOffset The amount to offset the overlay from the ingredient in the X direction.
	 *                May be negative, the overlay can be drawn larger than the ingredient.
	 * @param yOffset The amount to offset the overlay from the ingredient in the Y direction.
	 *                May be negative, the overlay can be drawn larger than the ingredient.
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset);

	/**
	 * Set the properties of this slot's fluid renderer.
	 * This will be used to render any fluid ingredients in the slot.
	 *
	 * If no fluid renderer is set, the default 16x16 renderer is used.
	 *
	 * @param capacity   maximum amount of fluid that this "tank" can hold
	 * @param showCapacity set `true` to show the capacity in the tooltip
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #setFluidRenderer(long, boolean, int, int)} instead.
	 */
	@Deprecated(forRemoval = true, since = "10.1.0")
	IRecipeSlotBuilder setFluidRenderer(int capacity, boolean showCapacity, int width, int height);

	/**
	 * Set the properties of this slot's fluid renderer.
	 * This will be used to render any fluid ingredients in the slot.
	 *
	 * If no fluid renderer is set, the default 16x16 renderer is used.
	 *
	 * @param capacity   maximum amount of fluid that this "tank" can hold
	 * @param showCapacity set `true` to show the capacity in the tooltip
	 *
	 * @since 10.1.0
	 */
	IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height);

	/**
	 * Set a custom renderer for the given ingredient type for this slot.
	 *
	 * If no custom renderer is set, the default 16x16 renderer from
	 * {@link IModIngredientRegistration#register} is used.
	 *
	 * @implNote if multiple renderers are set, they must all have the same
	 * {@link IIngredientRenderer#getWidth()} and
	 * {@link IIngredientRenderer#getWidth()}
	 * so that they can render together in rotation in the same space.
	 *
	 * @param ingredientType     the type of ingredient to use the custom renderer on
	 * @param ingredientRenderer the custom ingredient renderer to use for this type
	 *
	 * @since 9.3.0
	 */
	<T> IRecipeSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	);
}
