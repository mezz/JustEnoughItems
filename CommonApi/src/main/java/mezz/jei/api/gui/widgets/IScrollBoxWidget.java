package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A scrolling area with a scrollbar.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollBoxWidget}.
 *
 * @since 15.11.0
 */
@ApiStatus.NonExtendable
public interface IScrollBoxWidget extends IRecipeWidget, IJeiInputHandler {
	/**
	 * Set when the scrollbar is shown. Defaults to {@link ScrollbarVisibility#DEFAULT}.
	 * The content area keeps its width when the scrollbar is hidden.
	 *
	 * @since 15.69.0
	 */
	IScrollBoxWidget setScrollbarVisibility(ScrollbarVisibility visibility);

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
