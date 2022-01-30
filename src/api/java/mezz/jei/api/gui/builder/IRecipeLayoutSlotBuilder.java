package mezz.jei.api.gui.builder;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;

import javax.annotation.Nullable;

/**
 * Allows setting properties of a slot on a {@link IRecipeLayoutBuilder}.
 * Implements {@link IIngredientAcceptor} to add ingredients to the slot.
 *
 * @see IIngredientAcceptor
 *
 * @since JEI 9.3.0
 */
public interface IRecipeLayoutSlotBuilder extends IIngredientAcceptor<IRecipeLayoutSlotBuilder> {
	/**
	 * Set a custom background to draw behind the slot's ingredients.
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeLayoutSlotBuilder setBackground(IDrawable background);

	/**
	 * Set the properties of this slot's fluid renderer.
	 * This will be used to render any fluid ingredients in the slot.
	 *
	 * @param capacityMb   maximum amount of fluid that this "tank" can hold in milli-buckets
	 * @param showCapacity set `true` to show the capacity in the tooltip
	 * @param overlay      optional overlay to display over the tank.
	 * 	                   Typically, the overlay is fluid level lines,
	 * 	                   but it could also be a mask to shape the tank.
	 *
	 * @since JEI 9.3.0
	 */
	IRecipeLayoutSlotBuilder setFluidRenderer(int capacityMb, boolean showCapacity, @Nullable IDrawable overlay);

	/**
	 * Set a custom renderer for the given ingredient type for this slot.
	 *
	 * @param ingredientType     the type of ingredient to use the custom renderer on
	 * @param ingredientRenderer the custom ingredient renderer to use for this type
	 *
	 * @since JEI 9.3.0
	 */
	<T> IRecipeLayoutSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer);

	/**
	 * Set the rendering size of the ingredient and its background.
	 *
	 * @param width  the full width of the rendered ingredient and its background
	 * @param height the full height of the rendered ingredient and its background
	 */
	IRecipeLayoutSlotBuilder setSize(int width, int height);

	/**
	 * Set the ingredient inset relative to the background.
	 * This is useful when you want to render a background that is larger than the ingredient.
	 *
	 * @param xPadding the extra x offset added to the ingredient position relative to the background
	 * @param yPadding the extra y offset added to the ingredient position relative to the background
	 */
	IRecipeLayoutSlotBuilder setInnerPadding(int xPadding, int yPadding);

	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 *
	 * @see IRecipeSlotTooltipCallback
	 */
	IRecipeLayoutSlotBuilder addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);
}
