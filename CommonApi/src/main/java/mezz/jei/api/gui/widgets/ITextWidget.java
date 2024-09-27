package mezz.jei.api.gui.widgets;

import net.minecraft.client.gui.Font;

/**
 * An interface to allow configuration of a text widget.
 *
 * Add text to your recipe layout using {@link IRecipeExtrasBuilder#addText},
 * then configure it using this interface.
 *
 * By default, text is aligned to the top left, and uses the minecraft client font.
 *
 * @since 19.19.0
 */
public interface ITextWidget {
	/**
	 * Set the font used by this text widget when drawing text.
	 * Defaults to the minecraft client font.
	 *
	 * @since 19.19.0
	 */
	ITextWidget setFont(Font font);

	/**
	 * Set the color used by this text widget when drawing text.
	 * Defaults to black (0xFF000000)
	 *
	 * @since 19.19.0
	 */
	ITextWidget setColor(int color);

	/**
	 * Set the space in between lines of text, in pixels.
	 * Defaults to 2.
	 *
	 * @since 19.19.0
	 */
	ITextWidget setLineSpacing(int spacing);

	/**
	 * Set if the text should be drawn with a shadow.
	 * Defaults to false.
	 *
	 * @since 19.19.0
	 */
	ITextWidget setShadow(boolean shadow);

	/**
	 * Horizontally align text to the left within the given bounds. (default)
	 *
	 * @since 19.19.0
	 */
	ITextWidget alignHorizontalLeft();

	/**
	 * Horizontally align text in the center of the given bounds.
	 *
	 * @since 19.19.0
	 */
	ITextWidget alignHorizontalCenter();

	/**
	 * Horizontally align text to the right within the given bounds.
	 *
	 * @since 19.19.0
	 */
	ITextWidget alignHorizontalRight();

	/**
	 * Vertically align text to the top of the given bounds. (default)
	 *
	 * @since 19.19.0
	 */
	ITextWidget alignVerticalTop();

	/**
	 * Vertically align text in the center of the given bounds.
	 *
	 * @since 19.19.0
	 */
	ITextWidget alignVerticalCenter();

	/**
	 * Vertically align text to the bottom of the given bounds.
	 *
	 * @since 19.19.0
	 */
	ITextWidget alignVerticalBottom();
}
