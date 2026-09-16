package mezz.jei.test.client;

import mezz.jei.gui.input.GuiTextFieldFilter;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TextInputTestUtil {
	private TextInputTestUtil() {
	}

	public static void typePlainText(KeyboardHandler keyboardHandler, long windowHandle, GuiTextFieldFilter searchField) {
		String text = "stone";
		int[] keys = {GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_T, GLFW.GLFW_KEY_O, GLFW.GLFW_KEY_N, GLFW.GLFW_KEY_E};
		for (int i = 0; i < keys.length; i++) {
			char character = text.charAt(i);
			KeyEvent event = new KeyEvent(keys[i], 0, 0);
			try {
				invokeKeyPress(keyboardHandler, windowHandle, event);
				if (!searchField.isFocused()) {
					throw new AssertionError("Expected typing '" + character + "' to keep JEI's search field focused.");
				}
				invokeCharacterCallback(keyboardHandler, windowHandle, new CharacterEvent(character, 0));
				assertSearchText(searchField, text.substring(0, i + 1));
			} finally {
				invokeKeyEvent(keyboardHandler, windowHandle, GLFW.GLFW_RELEASE, event);
			}
		}
		searchField.setValue("");
	}

	public static void invokeKeyPress(KeyboardHandler keyboardHandler, long windowHandle, KeyEvent event) {
		invokeKeyEvent(keyboardHandler, windowHandle, GLFW.GLFW_PRESS, event);
	}

	private static void invokeKeyEvent(KeyboardHandler keyboardHandler, long windowHandle, int action, KeyEvent event) {
		try {
			Method method = KeyboardHandler.class.getDeclaredMethod("keyPress", long.class, int.class, KeyEvent.class);
			method.setAccessible(true);
			method.invoke(keyboardHandler, windowHandle, action, event);
		} catch (InvocationTargetException e) {
			throw new AssertionError("The Minecraft key callback failed.", e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to invoke Minecraft's key callback.", e);
		}
	}

	public static void invokeCharacterCallback(KeyboardHandler keyboardHandler, long windowHandle, CharacterEvent event) {
		try {
			Method method = KeyboardHandler.class.getDeclaredMethod("charTyped", long.class, CharacterEvent.class);
			method.setAccessible(true);
			method.invoke(keyboardHandler, windowHandle, event);
		} catch (InvocationTargetException e) {
			throw new AssertionError("The Minecraft character callback failed.", e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to invoke Minecraft's character callback.", e);
		}
	}

	private static void assertSearchText(GuiTextFieldFilter searchField, String expected) {
		if (!searchField.getValue().equals(expected)) {
			throw new AssertionError("Expected text input to produce '" + expected + "', got: " + searchField.getValue());
		}
	}

}
