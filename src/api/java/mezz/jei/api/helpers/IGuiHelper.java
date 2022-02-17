package mezz.jei.api.helpers;

import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.resources.ResourceLocation;

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
	 * Returns a slot drawable for drawing extra slots on guis
	 */
	IDrawableStatic getSlotDrawable();

	/**
	 * Returns a blank drawable for using as a blank recipe background.
	 */
	IDrawableStatic createBlankDrawable(int width, int height);

	/**
	 * Returns a 16x16 drawable for the given ingredient, matching the one JEI draws in the ingredient list.
	 * @since 9.1.1
	 */
	<V> IDrawable createDrawableIngredient(IIngredientType<V> type, V ingredient);

	/**
	 * Create a crafting grid helper.
	 * Helps set crafting-grid-style GuiItemStackGroup.
	 */
	ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1);

	/**
	 * Create a timer to help with rendering things that normally depend on ticks.
	 *
	 * @param ticksPerCycle the number of ticks for timer to run before starting over at 0
	 * @param maxValue      the number to count up to before starting over at 0
	 * @param countDown     if true, the tick timer will count backwards from maxValue
	 */
	ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown);

	/**
	 * Returns a 16x16 drawable for the given ingredient, matching the one JEI draws in the ingredient list.
	 * @deprecated Use {@link #createDrawableIngredient(IIngredientType, Object)}
	 */
	@Deprecated(forRemoval = true, since = "9.1.1")
	<V> IDrawable createDrawableIngredient(V ingredient);
}
