package mezz.jei.api.gui.builder;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows setting properties of a slot on a {@link IRecipeLayoutBuilder}.
 * Implements {@link IIngredientAcceptor} to add ingredients to the slot.
 *
 * @see IIngredientAcceptor for methods to add ingredients to this builder.
 *
 * @since 9.3.0
 */
@ApiStatus.NonExtendable
public interface IRecipeSlotBuilder extends IIngredientAcceptor<IRecipeSlotBuilder>, IPlaceable<IRecipeSlotBuilder> {
	/**
	 * Add a callback to alter the tooltip for these ingredients.
	 *
	 * @see mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #addRichTooltipCallback(IRecipeSlotRichTooltipCallback)}
	 */
	@SuppressWarnings("removal")
	@Deprecated(since = "19.8.5", forRemoval = true)
	IRecipeSlotBuilder addTooltipCallback(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback tooltipCallback);

	/**
	 * Add a callback to alter the rich tooltip for these ingredients.
	 *
	 * @see IRecipeSlotRichTooltipCallback
	 *
	 * @since 19.8.5
	 */
	IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback);

	/**
	 * Give the slot a unique name, for looking it up later by using
	 * {@link IRecipeSlotsView#findSlotByName(String)}
	 * in {@link IRecipeCategory#draw}
	 *
	 * @since 9.3.0
	 */
	IRecipeSlotBuilder setSlotName(String slotName);

	/**
	 * Set a normal slot background to draw behind the slot's ingredients.
	 * This background is 18x18 pixels and offset by (-1, -1) to match vanilla slots.
	 *
	 * @see IGuiHelper#getSlotDrawable() for the slot background drawable.
	 *
	 * @since 19.18.7
	 */
	IRecipeSlotBuilder setStandardSlotBackground();

	/**
	 * Set a normal slot background to draw behind the slot's ingredients.
	 * This background is 26x26 pixels and offset by (-5, -5) to match vanilla output slots.
	 *
	 * @see IGuiHelper#getOutputSlot() for the slot background drawable.
	 *
	 * @since 19.18.8
	 */
	IRecipeSlotBuilder setOutputSlotBackground();

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

	/**
	 * Convenience helper to add one Fluid ingredient.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * By default, fluids amounts below 1000 (i.e. one bucket) are rendered using a partial sprite,
	 * and fluid amounts above 1000 are rendered using a full sprite.
	 *
	 * The default renderer can be tweaked using {@link #setFluidRenderer}.
	 * For example, {@code .setFluidRenderer(1, false, 16, 16)} to always draw a full 16x16 sprite
	 * even if there is only a little fluid.
	 *
	 * To completely customize rendering, see {@link #setCustomRenderer(IIngredientType, IIngredientRenderer)}
	 *
	 * @see #addFluidStack(Fluid, long, DataComponentPatch) to add a Fluid with a {@link DataComponentPatch}.
	 * @since 11.1.0
	 */
	@Override
	IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount);

	/**
	 * Convenience helper to add one Fluid ingredient with a {@link DataComponentPatch}.
	 *
	 * To add multiple Fluid ingredients, you can call this multiple times.
	 *
	 * By default, fluids amounts below 1000 (i.e. one bucket) are rendered using a partial sprite,
	 * and fluid amounts above 1000 are rendered using a full sprite.
	 *
	 * The default renderer can be tweaked using {@link #setFluidRenderer}.
	 * For example, {@code .setFluidRenderer(1, false, 16, 16)} to always draw a full 16x16 sprite
	 * even if there is only a little fluid.
	 *
	 * To completely customize rendering, see {@link #setCustomRenderer(IIngredientType, IIngredientRenderer)}
	 *
	 * @see #addFluidStack(Fluid, long) to add a Fluid without a {@link DataComponentPatch}.
	 * @since 18.0.0
	 */
	@Override
	IRecipeSlotBuilder addFluidStack(Fluid fluid, long amount, DataComponentPatch component);
}
