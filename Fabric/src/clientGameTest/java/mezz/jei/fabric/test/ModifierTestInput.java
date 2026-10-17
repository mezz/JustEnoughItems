package mezz.jei.fabric.test;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

final class ModifierTestInput {
	private static final int[] KEYS = {GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_LEFT_SUPER};
	private static final int[] MASKS = {GLFW.GLFW_MOD_SHIFT, GLFW.GLFW_MOD_CONTROL, GLFW.GLFW_MOD_ALT, GLFW.GLFW_MOD_SUPER};

	private ModifierTestInput() {}

	static boolean matches(KeyMapping mapping, int key, int scanCode, int modifiers) {
		return withModifiers(modifiers, () -> mapping.matches(key, scanCode));
	}

	static boolean matchesMouse(KeyMapping mapping, int button, int modifiers) {
		return withModifiers(modifiers, () -> mapping.matchesMouse(button));
	}

	static boolean click(Screen screen, int button, int modifiers) {
		return withModifiers(modifiers, false, () -> screen.mouseClicked(1, 1, button));
	}

	private static <T> T withModifiers(int modifiers, Supplier<T> action) {
		return withModifiers(modifiers, true, action);
	}

	private static <T> T withModifiers(int modifiers, boolean dispatchKeys, Supplier<T> action) {
		boolean[] original = new boolean[KEYS.length];
		for (int i = 0; i < KEYS.length; i++) {
			original[i] = Boolean.TRUE.equals(FabricClientTestInput.getKeyState(KEYS[i]));
			set(KEYS[i], (modifiers & MASKS[i]) != 0, dispatchKeys);
		}
		try {
			return action.get();
		} finally {
			for (int i = 0; i < KEYS.length; i++) {
				set(KEYS[i], original[i], dispatchKeys);
			}
		}
	}

	private static void set(int key, boolean down, boolean dispatchKeys) {
		if (!dispatchKeys) {
			FabricClientTestInput.setKey(key, down);
			return;
		}
		if (Boolean.TRUE.equals(FabricClientTestInput.getKeyState(key)) == down) {
			return;
		}
		if (down) {
			FabricClientTestInput.holdKey(key);
		} else {
			FabricClientTestInput.releaseKey(key);
		}
	}
}
