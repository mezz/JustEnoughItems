package mezz.jei.api.gui.inputs;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * An input handler interface modeled after the vanilla {@link GuiEventListener}.
 * It has added support for passing in relative mouse positions when keys are pressed.
 *
 * For JEI-like input handling, use {@link IJeiInputHandler} instead.
 *
 * @since 15.9.0
 */
public interface IJeiGuiEventListener {
	/**
	 * Get the area covered by this handler relative to its parent element.
	 *
	 * Mouse coordinates passed to this handler are translated so that when
	 * the mouse is at this area's position, it is passed to this handler as if it were (0, 0).
	 *
	 * @since 15.9.0
	 */
	ScreenRectangle getArea();

	/**
	 * Called when the mouse is moved within the GUI element.
	 *
	 * @param mouseX the X coordinate of the mouse relative to the parent element.
	 * @param mouseY the Y coordinate of the mouse relative to the parent element.
	 *
	 * @since 15.9.0
	 */
	default void mouseMoved(double mouseX, double mouseY) {

	}

	/**
	 * Called when a mouse button is clicked within the GUI element.
	 *
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 *
	 * @param mouseX the X coordinate of the mouse relative to the parent element.
	 * @param mouseY the Y coordinate of the mouse relative to the parent element.
	 * @param button the button that was clicked.
	 *
	 * @since 15.9.0
	 */
	default boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	/**
	 * Called when a mouse button is released within the GUI element.
	 *
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 *
	 * @param mouseX the X coordinate of the mouse relative to the parent element.
	 * @param mouseY the Y coordinate of the mouse relative to the parent element.
	 * @param button the button that was released.
	 *
	 * @since 15.9.0
	 */
	default boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	/**
	 * Called when the mouse is dragged within the GUI element.
	 *
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 *
	 * @param mouseX the X coordinate of the mouse relative to the parent element.
	 * @param mouseY the Y coordinate of the mouse relative to the parent element.
	 * @param button the button that is being dragged.
	 * @param dragX  the X distance of the drag.
	 * @param dragY  the Y distance of the drag.
	 *
	 * @since 15.9.0
	 */
	default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}

	/**
	 * Called when the mouse is dragged within the GUI element.
	 *
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 *
	 * @param mouseX the X coordinate of the mouse relative to the parent element.
	 * @param mouseY the Y coordinate of the mouse relative to the parent element.
	 * @param scrollDelta the Y distance of the scroll.
	 *
	 * @since 15.9.0
	 */
	default boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		return false;
	}

	/**
	 * Called when a keyboard key is pressed within the GUI element.
	 *
	 * @return {@code true} if the event is consumed, {@code false} otherwise.
	 *
	 * @param mouseX the X coordinate of the mouse relative to the parent element.
	 * @param mouseY the Y coordinate of the mouse relative to the parent element.
	 * @param keyCode   the key code of the pressed key.
	 * @param scanCode  the scan code of the pressed key.
	 * @param modifiers the keyboard modifiers.
	 *
	 * @since 15.9.0
	 */
	default boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
		return false;
	}
}
