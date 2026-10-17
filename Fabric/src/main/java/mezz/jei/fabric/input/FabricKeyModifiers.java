package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Modifier combinations used only by JEI's Fabric key mappings. */
public final class FabricKeyModifiers {
	private static final int[] MASKS = {InputConstants.MOD_SHIFT, InputConstants.MOD_CONTROL, InputConstants.MOD_ALT, InputConstants.MOD_SUPER};
	private FabricKeyModifiers() {}

	public static int normalize(int modifiers) {
		int result = 0;
		for (int mask : MASKS) {
			if ((modifiers & mask) != 0) {
				result |= mask;
			}
		}
		return result;
	}

	public static int from(JeiKeyModifier modifier) {
		return switch (modifier) {
			case NONE -> 0;
			case SHIFT -> InputConstants.MOD_SHIFT;
			case CONTROL -> InputConstants.MOD_CONTROL;
			case ALT -> InputConstants.MOD_ALT;
			case CONTROL_OR_COMMAND -> {
				if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
					yield InputConstants.MOD_SUPER;
				}
				yield InputConstants.MOD_CONTROL;
			}
		};
	}

	public static int forKey(int key) {
		return switch (key) {
			case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> InputConstants.MOD_SHIFT;
			case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> InputConstants.MOD_CONTROL;
			case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> InputConstants.MOD_ALT;
			case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> InputConstants.MOD_SUPER;
			default -> 0;
		};
	}

	public static int current() {
		Minecraft minecraft = Minecraft.getInstance();
		int modifiers = 0;
		if (minecraft.hasShiftDown()) {
			modifiers |= InputConstants.MOD_SHIFT;
		}
		if (minecraft.hasControlDown()) {
			modifiers |= InputConstants.MOD_CONTROL;
		}
		if (minecraft.hasAltDown()) {
			modifiers |= InputConstants.MOD_ALT;
		}
		if (InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_SUPER)) {
			modifiers |= InputConstants.MOD_SUPER;
		}
		return modifiers;
	}

	public static Component getCombinedName(int modifiers, Component keyName) {
		if ((modifiers & InputConstants.MOD_ALT) != 0) {
			keyName = Component.translatable("jei.key.combo.alt", keyName);
		}
		if ((modifiers & InputConstants.MOD_SHIFT) != 0) {
			keyName = Component.translatable("jei.key.combo.shift", keyName);
		}
		if ((modifiers & InputConstants.MOD_CONTROL) != 0) {
			keyName = Component.translatable("jei.key.combo.control", keyName);
		}
		if ((modifiers & InputConstants.MOD_SUPER) != 0) {
			keyName = Component.translatable("jei.key.combo.command", keyName);
		}
		return keyName;
	}
}
