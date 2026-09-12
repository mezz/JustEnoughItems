package mezz.jei.common.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KeyNameUtil {
	private KeyNameUtil() {

	}

	/**
	 * The vanilla translation for left click is "LEFT BUTTON" and right click is "RIGHT BUTTON".
	 * We want better names for these in tooltips, and so use our own localization.
	 */
	public static Component getKeyDisplayName(InputConstants.Key key) {
		if (key.getType() == InputConstants.Type.MOUSE) {
			int value = key.getValue();
			if (value == InputConstants.MOUSE_BUTTON_LEFT) {
				return Component.translatable("jei.key.mouse.left");
			} else if (value == InputConstants.MOUSE_BUTTON_RIGHT) {
				return Component.translatable("jei.key.mouse.right");
			}
		}
		if (key.getType() == InputConstants.Type.KEYSYM) {
			int value = key.getValue();
			if (Minecraft.ON_OSX && (value == GLFW.GLFW_KEY_LEFT_SUPER || value == GLFW.GLFW_KEY_RIGHT_SUPER)) {
				return Component.translatable("jei.key.modifier.command");
			}
			return switch (value) {
				case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> Component.translatable("jei.key.modifier.shift");
				case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> Component.translatable("jei.key.modifier.alt");
				case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> Component.translatable("jei.key.modifier.control");
				default -> key.getDisplayName();
			};
		}
		return key.getDisplayName();
	}
}
