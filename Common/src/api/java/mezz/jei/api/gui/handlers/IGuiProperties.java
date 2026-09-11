package mezz.jei.api.gui.handlers;

import net.minecraft.client.gui.screens.Screen;

/**
 * Defines the properties of a GUI so that JEI can draw next to it.
 * Created by {@link IScreenHandler#apply(Screen)}
 *
 * @since 4.8.4
 */
public interface IGuiProperties {
	/**
	 * The screen class these properties describe.
	 *
	 * @since 19.5.1
	 */
	Class<? extends Screen> screenClass();

	/**
	 * The left edge of the GUI rectangle in screen coordinates.
	 *
	 * @since 19.5.1
	 */
	int guiLeft();

	/**
	 * The top edge of the GUI rectangle in screen coordinates.
	 *
	 * @since 19.5.1
	 */
	int guiTop();

	/**
	 * The width of the GUI rectangle.
	 *
	 * @since 19.5.1
	 */
	int guiXSize();

	/**
	 * The height of the GUI rectangle.
	 *
	 * @since 19.5.1
	 */
	int guiYSize();

	/**
	 * The width of the screen.
	 *
	 * @since 19.5.1
	 */
	int screenWidth();

	/**
	 * The height of the screen.
	 *
	 * @since 19.5.1
	 */
	int screenHeight();

	/**
	 * The right edge of the GUI rectangle in screen coordinates.
	 *
	 * @since 30.5.0
	 */
	default int guiRight() {
		return guiLeft() + guiXSize();
	}

	/**
	 * The bottom edge of the GUI rectangle in screen coordinates.
	 *
	 * @since 30.5.0
	 */
	default int guiBottom() {
		return guiTop() + guiYSize();
	}
}
