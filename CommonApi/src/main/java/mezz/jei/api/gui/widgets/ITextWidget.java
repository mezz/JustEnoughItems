package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.minecraft.client.gui.Font;

/**
 * An interface to allow configuration of a text widget.
 *
 * Add text to your recipe layout using {@link IRecipeExtrasBuilder#addText},
 * then configure it using this interface.
 *
 * By default, text is aligned to the top left, and uses the minecraft client font.
 *
 * @since 15.20.0
 */
public interface ITextWidget extends IPlaceable<ITextWidget> {
	/**
	 * Set the font used by this text widget when drawing text.
	 * Defaults to the minecraft client font.
	 *
	 * @since 15.20.0
	 */
	ITextWidget setFont(Font font);

	/**
	 * Set the color used by this text widget when drawing text.
	 * Defaults to black (0xFF000000)
	 *
	 * @since 15.20.0
	 */
	ITextWidget setColor(int color);

	/**
	 * Set the space in between lines of text, in pixels.
	 * Defaults to 2.
	 *
	 * @since 15.20.0
	 */
	ITextWidget setLineSpacing(int spacing);

	/**
	 * Set if the text should be drawn with a shadow.
	 * Defaults to false.
	 *
	 * @since 15.20.0
	 */
	ITextWidget setShadow(boolean shadow);

	/**
	 * Set the horizontal alignment of the text within the {@link #getWidth()} area.
	 * The default setting is {@link HorizontalAlignment#LEFT}.
	 *
	 * @since 15.20.0
	 */
	ITextWidget setTextAlignment(HorizontalAlignment horizontalAlignment);

	/**
	 * Set the vertical alignment of the text within the {@link #getHeight()} area.
	 * The default setting is {@link VerticalAlignment#TOP}.
	 *
	 * @since 15.20.0
	 */
	ITextWidget setTextAlignment(VerticalAlignment verticalAlignment);
}
