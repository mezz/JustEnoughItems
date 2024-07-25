package mezz.jei.common.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

public class KeyNameUtil {
	/**
	 * The vanilla translation for left click is "LEFT BUTTON" and right click is "RIGHT BUTTON".
	 * We want better names for these in tooltips, and so use our own localization.
	 */
	public static Component getKeyDisplayName(InputConstants.Key key) {
		// The vanilla translation for left click is "LEFT BUTTON" and right click is "RIGHT BUTTON".
		// We want better names for these in tooltips, and so use our own localization.
		if (key.getType() == InputConstants.Type.MOUSE) {
			int value = key.getValue();
			if (value == InputConstants.MOUSE_BUTTON_LEFT) {
				return Component.translatable("jei.key.mouse.left");
			} else if (value == InputConstants.MOUSE_BUTTON_RIGHT) {
				return Component.translatable("jei.key.mouse.right");
			}
		}
		return key.getDisplayName();
	}
}
