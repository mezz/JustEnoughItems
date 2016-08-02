package mezz.jei.api;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;

/**
 * Helps with the implementation of GUIs.
 * Get the instance from {@link IJeiHelpers#getGuiHelper()}.
 */
public interface IGuiHelper {

	@Nonnull
	IDrawableStatic createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height);

	@Nonnull
	IDrawableStatic createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight);

	/**
	 * Creates an animated texture for a gui, revealing the texture over time.
	 *
	 * @param drawable       the underlying texture to draw
	 * @param ticksPerCycle  the number of ticks for the animation to run before starting over
	 * @param startDirection the direction that the animation starts drawing the texture
	 * @param inverted       when inverted is true, the texture will start fully drawn and be hidden over time
	 */
	@Nonnull
	IDrawableAnimated createAnimatedDrawable(@Nonnull IDrawableStatic drawable, int ticksPerCycle, @Nonnull IDrawableAnimated.StartDirection startDirection, boolean inverted);

	/**
	 * Returns a slot drawable for drawing extra slots on guis
	 */
	@Nonnull
	IDrawableStatic getSlotDrawable();

	/**
	 * Returns a blank drawable for using as a blank recipe background.
	 */
	@Nonnull
	IDrawableStatic createBlankDrawable(int width, int height);

	/**
	 * Create a crafting grid helper.
	 * Helps set crafting-grid-style GuiItemStackGroup.
	 */
	@Nonnull
	ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot);

	/**
	 * Create a timer to help with rendering things that normally depend on ticks.
	 *
	 * @param ticksPerCycle the number of ticks for timer to run before starting over at 0
	 * @param maxValue      the number to count up to before starting over at 0
	 * @param countDown     if true, the tick timer will count backwards from maxValue
	 */
	@Nonnull
	ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown);
}
