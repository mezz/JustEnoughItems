package mezz.jei.api.gui;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;

/**
 * Builder for creating drawables from a resource location.
 * Create an instance with {@link IGuiHelper#drawableBuilder(ResourceLocation, int, int, int, int)}
 *
 * @since JEI 4.11.0
 */
public interface IDrawableBuilder {
	/**
	 * For textures that are not 256x256, specify the size.
	 */
	IDrawableBuilder setTextureSize(int width, int height);

	/**
	 * Add extra blank space around the texture by adjusting the padding.
	 */
	IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight);

	/**
	 * Remove blank space around the texture by trimming it.
	 *
	 * @since JEI 4.14.4
	 */
	IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight);

	/**
	 * Creates a normal, non-animated drawable.
	 */
	IDrawableStatic build();

	/**
	 * Creates an animated texture for a gui, revealing the texture over time.
	 *
	 * @param ticksPerCycle  the number of ticks for the animation to run before starting over
	 * @param startDirection the direction that the animation starts drawing the texture
	 * @param inverted       when inverted is true, the texture will start fully drawn and be hidden over time
	 */
	IDrawableAnimated buildAnimated(int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted);

	/**
	 * Creates an animated texture for a gui, revealing the texture over time.
	 *
	 * @param tickTimer      a custom tick timer, used for advanced control over the animation
	 * @param startDirection the direction that the animation starts drawing the texture
	 */
	IDrawableAnimated buildAnimated(ITickTimer tickTimer, IDrawableAnimated.StartDirection startDirection);
}
