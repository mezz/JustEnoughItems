package mezz.jei.api.gui.buttons;

import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * A snapshot of a JEI button's bounds and state.
 *
 * @param bounds the bounds in GUI-scaled screen coordinates
 * @param visible whether the button is drawn
 * @param active whether the button is enabled, regardless of visibility
 *
 * @since 31.22.0
 */
public record ButtonSnapshot(ScreenRectangle bounds, boolean visible, boolean active) {
}
