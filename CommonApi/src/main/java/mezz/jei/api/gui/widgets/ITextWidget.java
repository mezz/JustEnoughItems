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
	 * @since 15.20.1
	 */
	ITextWidget setTextAlignment(HorizontalAlignment horizontalAlignment);

	/**
	 * Set the vertical alignment of the text within the {@link #getHeight()} area.
	 * The default setting is {@link VerticalAlignment#TOP}.
	 *
	 * @since 15.20.1
	 */
	ITextWidget setTextAlignment(VerticalAlignment verticalAlignment);

	/**
	 * Horizontally align text to the left within the given bounds. (default)
	 *
	 * @since 15.20.0
	 * @deprecated use {@link #setTextAlignment(HorizontalAlignment)}
	 */
	@Deprecated(since = "15.20.1", forRemoval = true)
	default ITextWidget alignHorizontalLeft() {
		return setTextAlignment(HorizontalAlignment.LEFT);
	}

	/**
	 * Horizontally align text in the center of the given bounds.
	 *
	 * @since 15.20.0
	 * @deprecated use {@link #setTextAlignment(HorizontalAlignment)}
	 */
	@Deprecated(since = "15.20.1", forRemoval = true)
	default ITextWidget alignHorizontalCenter() {
		return setTextAlignment(HorizontalAlignment.CENTER);
	}

	/**
	 * Horizontally align text to the right within the given bounds.
	 *
	 * @since 15.20.0
	 * @deprecated use {@link #setTextAlignment(HorizontalAlignment)}
	 */
	@Deprecated(since = "15.20.1", forRemoval = true)
	default ITextWidget alignHorizontalRight() {
		return setTextAlignment(HorizontalAlignment.RIGHT);
	}

	/**
	 * Vertically align text to the top of the given bounds. (default)
	 *
	 * @since 15.20.0
	 * @deprecated use {@link #setTextAlignment(VerticalAlignment)}
	 */
	@Deprecated(since = "15.20.1", forRemoval = true)
	default ITextWidget alignVerticalTop() {
		return setTextAlignment(VerticalAlignment.TOP);
	}

	/**
	 * Vertically align text in the center of the given bounds.
	 *
	 * @since 15.20.0
	 * @deprecated use {@link #setTextAlignment(VerticalAlignment)}
	 */
	@Deprecated(since = "15.20.1", forRemoval = true)
	default ITextWidget alignVerticalCenter() {
		return setTextAlignment(VerticalAlignment.CENTER);
	}

	/**
	 * Vertically align text to the bottom of the given bounds.
	 *
	 * @since 15.20.0
	 * @deprecated use {@link #setTextAlignment(VerticalAlignment)}
	 */
	@Deprecated(since = "15.20.1", forRemoval = true)
	default ITextWidget alignVerticalBottom() {
		return setTextAlignment(VerticalAlignment.BOTTOM);
	}
}
