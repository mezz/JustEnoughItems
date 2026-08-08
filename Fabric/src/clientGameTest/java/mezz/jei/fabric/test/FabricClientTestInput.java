package mezz.jei.fabric.test;

import mezz.jei.common.input.keys.JeiKeyModifier;
import mezz.jei.fabric.test.mixin.KeyboardHandlerAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test-only input state used when Fabric's client gametest input API is unavailable.
 */
public final class FabricClientTestInput {
	private static final Map<Integer, Boolean> KEY_STATES = new ConcurrentHashMap<>();

	private FabricClientTestInput() {

	}

	public static void holdModifier(JeiKeyModifier modifier) {
		setModifier(modifier, true);
	}

	public static void releaseModifier(JeiKeyModifier modifier) {
		setModifier(modifier, false);
	}

	public static void clear() {
		KEY_STATES.clear();
	}

	public static void pressKey(KeyMapping keyMapping) {
		var key = KeyBindingHelper.getBoundKeyOf(keyMapping);
		if (key.getType() != com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM) {
			throw new IllegalArgumentException("Expected a keyboard mapping, got: " + key.getName());
		}
		pressKey(key.getValue());
	}

	public static void pressKey(int key) {
		ClientTestUtil.runOnClient(client -> {
			invokeKeyPress(client, key, GLFW.GLFW_PRESS);
			invokeKeyPress(client, key, GLFW.GLFW_RELEASE);
		});
	}

	public static void typeChar(char codepoint) {
		ClientTestUtil.runOnClient(client -> {
			long window = client.getWindow().getWindow();
			((KeyboardHandlerAccessor) client.keyboardHandler).jei$invokeCharTyped(window, codepoint, 0);
		});
	}

	public static Boolean getKeyState(int key) {
		return KEY_STATES.get(key);
	}

	private static void setModifier(JeiKeyModifier modifier, boolean pressed) {
		switch (modifier) {
			case CONTROL_OR_COMMAND -> setKey(Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL, pressed);
			case SHIFT -> setKey(GLFW.GLFW_KEY_LEFT_SHIFT, pressed);
			case ALT -> setKey(GLFW.GLFW_KEY_LEFT_ALT, pressed);
			default -> throw new IllegalArgumentException("Unsupported test modifier: " + modifier);
		}
	}

	private static void setKey(int key, boolean pressed) {
		if (pressed) {
			KEY_STATES.put(key, true);
		} else {
			KEY_STATES.remove(key);
		}
	}

	private static void invokeKeyPress(Minecraft client, int key, int action) {
		long window = client.getWindow().getWindow();
		((KeyboardHandlerAccessor) client.keyboardHandler).jei$invokeKeyPress(window, key, 0, action, 0);
	}
}
