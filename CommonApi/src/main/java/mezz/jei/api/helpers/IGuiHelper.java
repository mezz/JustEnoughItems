package mezz.jei.api.helpers;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Helps with the implementation of GUIs.
 * Get the instance from {@link IJeiHelpers#getGuiHelper()}.
 */
public interface IGuiHelper {
	/**
	 * Create a drawable from part of a standard 256x256 gui texture.
	 *
	 * If your texture is not exactly 256x256, you will need to create a `{@link IDrawableBuilder} instead
	 * with {@link #drawableBuilder(ResourceLocation, int, int, int, int)}
	 * and then specify the texture size with {@link IDrawableBuilder#setTextureSize(int, int)}
	 */
	default IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return drawableBuilder(resourceLocation, u, v, width, height).build();
	}

	/**
	 * Create a {@link IDrawableBuilder} which gives more control over drawable creation.
	 *
	 * @return a new {@link IDrawableBuilder} with the given resource location
	 */
	IDrawableBuilder drawableBuilder(ResourceLocation resourceLocation, int u, int v, int width, int height);

	/**
	 * Creates an animated texture for a gui, revealing the texture over time.
	 *
	 * @param drawable       the underlying texture to draw
	 * @param ticksPerCycle  the number of ticks for the animation to run before starting over
	 * @param startDirection the direction that the animation starts drawing the texture
	 * @param inverted       when inverted is true, the texture will start fully drawn and be hidden over time
	 */
	IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted);

	/**
	 * Creates an animated texture for a gui, revealing the texture over time.
	 *
	 * @param drawable       the underlying texture to draw
	 * @param tickTimer      a timer to help render things that normally depend on ticks
	 * @param startDirection the direction that the animation starts drawing the texture
	 *
	 * @since 19.18.8
	 */
	IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection);

	/**
	 * Returns a vanilla-style slot for drawing on guis.
	 */
	IDrawableStatic getSlotDrawable();

	/**
	 * Returns a vanilla-style large output slot for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableStatic getOutputSlot();

	/**
	 * Returns a vanilla-style recipe arrow for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableStatic getRecipeArrow();

	/**
	 * Returns a vanilla-style filled (white) recipe arrow for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableStatic getRecipeArrowFilled();

	/**
	 * Returns a vanilla-style recipe arrow that fills over time, for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableAnimated createAnimatedRecipeArrow(int ticksPerCycle);

	/**
	 * Returns a vanilla-style grey plus sign for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableStatic getRecipePlusSign();

	/**
	 * Returns a vanilla-style recipe flame (red) for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableStatic getRecipeFlameFilled();

	/**
	 * Returns a vanilla-style recipe flame background (grey) for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableStatic getRecipeFlameEmpty();

	/**
	 * Returns a vanilla-style recipe flame that empties over time, for drawing on guis.
	 *
	 * @since 19.18.8
	 */
	IDrawableAnimated createAnimatedRecipeFlame(int ticksPerCycle);

	/**
	 * Returns a blank drawable for using as a blank recipe background.
	 */
	IDrawableStatic createBlankDrawable(int width, int height);

	/**
	 * Returns a 16x16 drawable for the given ItemStack,
	 * matching the one JEI draws in the ingredient list.
	 *
	 * @see #createDrawableIngredient(IIngredientType, Object) for other ingredient types.
	 * @since 11.1.1
	 */
	default IDrawable createDrawableItemStack(ItemStack ingredient) {
		return createDrawableIngredient(VanillaTypes.ITEM_STACK, ingredient);
	}

	/**
	 * Returns a 16x16 drawable for the given ItemLike,
	 * matching the one JEI draws in the ingredient list.
	 *
	 * @see #createDrawableIngredient(IIngredientType, Object) for other ingredient types.
	 * @since 19.18.1
	 */
	default IDrawable createDrawableItemLike(ItemLike itemLike) {
		return createDrawableIngredient(VanillaTypes.ITEM_STACK, itemLike.asItem().getDefaultInstance());
	}

	/**
	 * Returns a 16x16 drawable for the given ingredient,
	 * matching the one JEI draws in the ingredient list.
	 * @since 9.1.1
	 */
	<V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient);

	/**
	 * Returns a 16x16 drawable for the given ingredient,
	 * matching the one JEI draws in the ingredient list.
	 * @since 19.1.0
	 */
	<V> IDrawable createDrawableIngredient(ITypedIngredient<V> ingredient);

	/**
	 * Create a crafting grid helper.
	 * Helps set crafting-grid-style GuiItemStackGroup.
	 */
	ICraftingGridHelper createCraftingGridHelper();

	/**
	 * Create a scroll grid widget factory.
	 * Handles displaying a grid of ingredient slots in a scrolling area.
	 *
	 * Add ingredients to it using {@link IRecipeLayoutBuilder#addSlotToWidget(RecipeIngredientRole, mezz.jei.api.gui.widgets.ISlottedWidgetFactory)}
	 *
	 * @since 19.7.0
	 * @deprecated use {@link IRecipeExtrasBuilder#addScrollGridWidget} instead, it's much simpler
	 */
	@SuppressWarnings("removal")
	@Deprecated(since = "19.19.3", forRemoval = true)
	mezz.jei.api.gui.widgets.IScrollGridWidgetFactory<?> createScrollGridFactory(int columns, int visibleRows);

	/**
	 * Create a scroll box widget.
	 * Handles displaying drawable contents in a scrolling area.
	 *
	 * @since 19.8.0
	 *
	 * @deprecated use {@link IRecipeExtrasBuilder#addScrollBoxWidget}
	 */
	@Deprecated(since = "19.18.9", forRemoval = true)
	IScrollBoxWidget createScrollBoxWidget(IDrawable contents, int visibleHeight, int xPos, int yPos);

	/**
	 * Create a scroll box widget.
	 * Handles displaying drawable contents in a scrolling area.
	 *
	 * @since 19.18.10
	 */
	IScrollBoxWidget createScrollBoxWidget(int width, int height, int xPos, int yPos);

	/**
	 * The amount of extra horizontal space that a {@link IScrollBoxWidget} takes up with its scroll bar.
	 *
	 * @since 19.8.0
	 *
	 * @deprecated use {@link IRecipeExtrasBuilder#addScrollBoxWidget}
	 */
	@Deprecated(since = "19.18.9", forRemoval = true)
	int getScrollBoxScrollbarExtraWidth();

	/**
	 * Create a simple widget from an {@link IDrawable}
	 *
	 * @since 19.18.8
	 */
	IRecipeWidget createWidgetFromDrawable(IDrawable drawable, int xPos, int yPos);

	/**
	 * Create a timer to help with rendering things that normally depend on ticks.
	 *
	 * @param ticksPerCycle the number of ticks for timer to run before starting over at 0
	 * @param maxValue      the number to count up to before starting over at 0
	 * @param countDown     if true, the tick timer will count backwards from maxValue
	 */
	ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown);
}
