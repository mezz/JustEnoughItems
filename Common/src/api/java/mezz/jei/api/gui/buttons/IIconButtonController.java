package mezz.jei.api.gui.buttons;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;

/**
 * Controller interface for an icon-based button.
 *
 * <p>
 * Implementations are responsible for handling user input and updating
 * the visual and interactive state of the button via {@link IButtonState}.
 * </p>
 *
 * <p>
 * This interface is designed to separate button behavior from rendering
 * and layout concerns.
 * </p>
 *
 * @since 27.2.0
 */
public interface IIconButtonController {
	/**
	 * Called when the button receives an input.
	 *
	 * @param input the user input that triggered the press
	 * @return true if the input was handled and should not propagate further
	 *
	 * @since 27.2.0
	 */
	boolean onPress(IJeiUserInput input);

	/**
	 * Adds tooltip text for this button.
	 *
	 * <p>
	 * This method may be called every frame while the button is hovered.
	 * Implementations should avoid expensive logic.
	 * </p>
	 *
	 * @param tooltip the tooltip builder to add lines to
	 *
	 * @since 27.2.0
	 */
	default void getTooltips(ITooltipBuilder tooltip) {

	}

	/**
	 * Initializes the button state.
	 *
	 * <p>
	 * This method is called once when the button is created.
	 * </p>
	 *
	 * <p>
	 * The default implementation delegates to {@link #updateState(IButtonState)}
	 * so that implementations can place all state logic in a single method
	 * when no special one-time initialization is required.
	 * </p>
	 *
	 * @param state the mutable button state
	 *
	 * @since 27.2.0
	 */
	default void initState(IButtonState state) {
		updateState(state);
	}

	/**
	 * Updates the button state every tick and after user inputs.
	 *
	 * <p>
	 * This method may be called repeatedly to reflect changes in application
	 * or GUI state.
	 * </p>
	 *
	 * @param state the mutable button state
	 *
	 * @since 27.2.0
	 */
	default void updateState(IButtonState state) {

	}

	/**
	 * Draws additional visuals for the button.
	 *
	 * <p>
	 * This is intended for overlays such as highlights, indicators,
	 * or debug visuals, and is rendered after the button itself.
	 * </p>
	 *
	 * @param guiGraphics the current gui graphics instance
	 * @param buttonArea the screen area occupied by the button
	 * @param mouseX the mouse x position
	 * @param mouseY the mouse y position
	 * @param partialTicks the partial tick time
	 *
	 * @since 27.2.0
	 */
	default void drawExtras(GuiGraphicsExtractor guiGraphics, Rect2i buttonArea, int mouseX, int mouseY, float partialTicks) {

	}
}
