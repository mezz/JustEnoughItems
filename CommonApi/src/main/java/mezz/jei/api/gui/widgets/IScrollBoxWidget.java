package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A smooth-scrolling area with a scrollbar.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollBoxWidget}.
 *
 * @since 11.33.0
 */
@ApiStatus.NonExtendable
public interface IScrollBoxWidget extends IRecipeWidget, IJeiInputHandler {
	/**
	 * Get the width available for displaying contents in the scroll box.
	 * The scroll bar takes up some of the space, so this can be used in order to create accurately-sized contents.
	 *
	 * @since 11.38.0
	 */
	int getContentAreaWidth();

	/**
	 * Get the visible height for displaying contents in the scroll box.
	 * The actual height of the contents can be taller, because the box can scroll to show more.
	 *
	 * @since 11.38.0
	 */
	int getContentAreaHeight();

	/**
	 * Set the contents to display inside the scroll box.
	 * The drawable width should match {@link #getContentAreaWidth()}, and the height can be any height.
	 *
	 * @since 11.38.0
	 */
	IScrollBoxWidget setContents(IDrawable contents);

	/**
	 * Display text in the scroll box.
	 * Text will be automatically wrapped in order to fit inside of {@link #getContentAreaWidth()}.
	 *
	 * @since 11.38.0
	 */
	IScrollBoxWidget setContents(List<FormattedText> text);
}
