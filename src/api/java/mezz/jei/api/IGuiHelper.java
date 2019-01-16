package mezz.jei.api;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;

/**
 * Helps with the implementation of GUIs.
 * Get the instance from {@link IJeiHelpers#getGuiHelper()}.
 */
public interface IGuiHelper {
	/**
	 * Create a drawable from part of a standard 256x256 gui texture.
	 */
	default IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return drawableBuilder(resourceLocation, u, v, width, height).build();
	}

	/**
	 * Create a {@link IDrawableBuilder} which gives more control over drawable creation.
	 *
	 * @return a new {@link IDrawableBuilder} with the given resource location
	 * @since JEI 4.11.0
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
	 *
	 * @since JEI 4.9.1
	 */
	<V> IDrawable createDrawableIngredient(V ingredient);

	/**
	 * Create a crafting grid helper.
	 * Helps set crafting-grid-style GuiItemStackGroup.
	 */
	ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot);

	/**
	 * Create a timer to help with rendering things that normally depend on ticks.
	 *
	 * @param ticksPerCycle the number of ticks for timer to run before starting over at 0
	 * @param maxValue      the number to count up to before starting over at 0
	 * @param countDown     if true, the tick timer will count backwards from maxValue
	 */
	ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown);

	/**
	 * Create a drawable from part of an arbitrary sized texture.
	 *
	 * @since JEI 4.0.1
	 * @deprecated since JEI 4.11.0. Use {@link #drawableBuilder(ResourceLocation, int, int, int, int)}
	 */
	@Deprecated
	default IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height, int textureWidth, int textureHeight) {
		return drawableBuilder(resourceLocation, u, v, width, height)
			.setTextureSize(textureWidth, textureHeight)
			.build();
	}

	/**
	 * Create a drawable from part of a 256x256 texture.
	 * Padding arguments create extra space around the texture.
	 *
	 * @deprecated since JEI 4.11.0. Use {@link #drawableBuilder(ResourceLocation, int, int, int, int)}
	 */
	@Deprecated
	default IDrawableStatic createDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		return drawableBuilder(resourceLocation, u, v, width, height)
			.addPadding(paddingTop, paddingBottom, paddingLeft, paddingRight)
			.build();
	}

}
