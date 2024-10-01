package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

/**
 * A smooth-scrolling area with a scrollbar.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollBoxWidget}.
 *
 * @since 15.11.0
 */
public interface IScrollBoxWidget extends IRecipeWidget, IJeiInputHandler {
	/**
	 * Get the width available for displaying contents in the scroll box.
	 * The scroll bar takes up some of the space, so this can be used in order to create accurately-sized contents.
	 *
	 * @since 15.20.0
	 */
	int getContentAreaWidth();

	/**
	 * Get the visible height for displaying contents in the scroll box.
	 * The actual height of the contents can be taller, because the box can scroll to show more.
	 *
	 * @since 15.20.0
	 */
	int getContentAreaHeight();

	/**
	 * Set the contents to display inside the scroll box.
	 * The drawable width should match {@link #getContentAreaWidth()}, and the height can be any height.
	 *
	 * @since 15.20.0
	 */
	IScrollBoxWidget setContents(IDrawable contents);

	/**
	 * Display text in the scroll box.
	 * Text will be automatically wrapped in order to fit inside of {@link #getContentAreaWidth()}.
	 *
	 * @since 15.20.0
	 */
	IScrollBoxWidget setContents(List<FormattedText> text);
}
