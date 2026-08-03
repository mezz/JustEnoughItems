package mezz.jei.fabric.test;

import mezz.jei.common.input.keys.JeiKeyModifier;
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

	public static void holdKey(int key) {
		setKey(key, true);
	}

	public static void releaseKey(int key) {
		setKey(key, false);
	}

	public static void clear() {
		KEY_STATES.clear();
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
}
