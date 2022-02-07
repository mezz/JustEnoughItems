package mezz.jei.api.gui.builder;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;

/**
 * Allows setting properties of a slot on a {@link IRecipeLayoutBuilder}.
 * Implements {@link IIngredientAcceptor} to add ingredients to the slot.
 *
 * @see IIngredientAcceptor
 *
 * @since JEI 9.3.0
 */
public interface IRecipeSlotBuilder extends IIngredientAcceptor<IRecipeSlotBuilder> {
	/**
	 * Set a custom background to draw behind the slot's ingredients.
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder setBackground(IDrawable background);

	/**
	 * Set the properties of this slot's fluid renderer.
	 * This will be used to render any fluid ingredients in the slot.
	 *
	 * @param capacityMb   maximum amount of fluid that this "tank" can hold in milli-buckets
	 * @param showCapacity set `true` to show the capacity in the tooltip
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder setFluidRenderer(int capacityMb, boolean showCapacity);

	/**
	 * Set a custom renderer for the given ingredient type for this slot.
	 *
	 * @param ingredientType     the type of ingredient to use the custom renderer on
	 * @param ingredientRenderer the custom ingredient renderer to use for this type
	 *
	 * @since JEI 9.3.0
	 */
	<T> IRecipeSlotBuilder setCustomRenderer(
		IIngredientType<T> ingredientType,
		IIngredientRenderer<T> ingredientRenderer
	);

	/**
	 * Set an overlay to draw on top of the slot's ingredient.
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder setOverlay(IDrawable overlay);

	/**
	 * Set the rendering size of the ingredient and its background.
	 *
	 * @param width  the full width of the rendered ingredient and its background
	 * @param height the full height of the rendered ingredient and its background
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder setSize(int width, int height);

	/**
	 * Set the ingredient inset relative to the background.
	 * This is useful when you want to render a background that is larger than the ingredient.
	 *
	 * @param xPadding the extra x offset added to the ingredient position relative to the background
	 * @param yPadding the extra y offset added to the ingredient position relative to the background
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder setInnerPadding(int xPadding, int yPadding);

	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 *
	 * @see IRecipeSlotTooltipCallback
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);

	/**
	 * Give the slot a unique name, for use by {@link IRecipeSlotsView#findSlotByName(String)}
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeSlotBuilder setSlotName(String slotName);
}
