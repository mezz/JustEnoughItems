package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Modifier combinations used only by JEI's Fabric key mappings. */
public final class FabricKeyModifiers {
	private static final int[] MASKS = {GLFW.GLFW_MOD_SHIFT, GLFW.GLFW_MOD_CONTROL, GLFW.GLFW_MOD_ALT, GLFW.GLFW_MOD_SUPER};
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
			case SHIFT -> GLFW.GLFW_MOD_SHIFT;
			case CONTROL -> GLFW.GLFW_MOD_CONTROL;
			case ALT -> GLFW.GLFW_MOD_ALT;
			case CONTROL_OR_COMMAND -> {
				if (Minecraft.ON_OSX) {
					yield GLFW.GLFW_MOD_SUPER;
				}
				yield GLFW.GLFW_MOD_CONTROL;
			}
		};
	}

	public static int forKey(int key) {
		return switch (key) {
			case InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT -> GLFW.GLFW_MOD_SHIFT;
			case InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL -> GLFW.GLFW_MOD_CONTROL;
			case InputConstants.KEY_LALT, InputConstants.KEY_RALT -> GLFW.GLFW_MOD_ALT;
			case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> GLFW.GLFW_MOD_SUPER;
			default -> 0;
		};
	}

	public static int current() {
		Minecraft minecraft = Minecraft.getInstance();
		int modifiers = 0;
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RSHIFT)) {
			modifiers |= GLFW.GLFW_MOD_SHIFT;
		}
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RCONTROL)) {
			modifiers |= GLFW.GLFW_MOD_CONTROL;
		}
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_LALT) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), InputConstants.KEY_RALT)) {
			modifiers |= GLFW.GLFW_MOD_ALT;
		}
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SUPER)) {
			modifiers |= GLFW.GLFW_MOD_SUPER;
		}
		return modifiers;
	}

	public static Component getCombinedName(int modifiers, Component keyName) {
		if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
			keyName = Component.translatable("jei.key.combo.alt", keyName);
		}
		if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
			keyName = Component.translatable("jei.key.combo.shift", keyName);
		}
		if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
			keyName = Component.translatable("jei.key.combo.control", keyName);
		}
		if ((modifiers & GLFW.GLFW_MOD_SUPER) != 0) {
			keyName = Component.translatable("jei.key.combo.command", keyName);
		}
		return keyName;
	}
}
