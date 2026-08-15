package mezz.jei.test.client;

import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiTextFieldFilter;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.IMEPreeditOverlay;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.PreeditEvent;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public final class ImeTextInputTestUtil {
	private ImeTextInputTestUtil() {

	}

	public static void typeKoreanText(
		KeyboardHandler keyboardHandler,
		long windowHandle,
		GuiTextFieldFilter searchField
	) {
		ReflectionUtil reflectionUtil = new ReflectionUtil();

		updateImeComposition(keyboardHandler, windowHandle, "ㅎ", "하", "한");
		assertSearchText(searchField, "");
		assertPreeditOverlay(searchField, reflectionUtil, true);
		commitImeComposition(keyboardHandler, windowHandle, '한');
		assertSearchText(searchField, "한");
		assertPreeditOverlay(searchField, reflectionUtil, false);

		updateImeComposition(keyboardHandler, windowHandle, "ㄱ", "그", "글");
		assertSearchText(searchField, "한");
		assertPreeditOverlay(searchField, reflectionUtil, true);
		commitImeComposition(keyboardHandler, windowHandle, '글');
		assertSearchText(searchField, "한글");
		assertPreeditOverlay(searchField, reflectionUtil, false);
	}

	public static void invokeKeyPress(KeyboardHandler keyboardHandler, long windowHandle, KeyEvent event) {
		try {
			Method method = KeyboardHandler.class.getDeclaredMethod("keyPress", long.class, int.class, KeyEvent.class);
			method.setAccessible(true);
			method.invoke(keyboardHandler, windowHandle, GLFW.GLFW_PRESS, event);
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

	private static void assertPreeditOverlay(
		GuiTextFieldFilter searchField,
		ReflectionUtil reflectionUtil,
		boolean expected
	) {
		boolean hasPreeditOverlay = reflectionUtil.getFieldWithClass(searchField, IMEPreeditOverlay.class)
			.findAny()
			.isPresent();
		if (hasPreeditOverlay != expected) {
			throw new AssertionError("Expected the search field's IME composition overlay presence to be: " + expected);
		}
	}

	private static void assertSearchText(GuiTextFieldFilter searchField, String expected) {
		if (!searchField.getValue().equals(expected)) {
			throw new AssertionError("Expected consecutive IME input to produce '" + expected + "', got: " + searchField.getValue());
		}
	}

	private static void updateImeComposition(KeyboardHandler keyboardHandler, long windowHandle, String... stages) {
		for (String stage : stages) {
			PreeditEvent event = new PreeditEvent(stage, stage.length(), List.of(stage), 0);
			invokePreeditCallback(keyboardHandler, windowHandle, event);
		}
	}

	private static void commitImeComposition(KeyboardHandler keyboardHandler, long windowHandle, char character) {
		invokeCharacterCallback(keyboardHandler, windowHandle, new CharacterEvent(character));
		invokePreeditCallback(keyboardHandler, windowHandle, null);
	}

	private static void invokePreeditCallback(KeyboardHandler keyboardHandler, long windowHandle, @Nullable PreeditEvent event) {
		try {
			Method method = KeyboardHandler.class.getDeclaredMethod("preeditCallback", long.class, PreeditEvent.class);
			method.setAccessible(true);
			method.invoke(keyboardHandler, windowHandle, event);
		} catch (InvocationTargetException e) {
			throw new AssertionError("The Minecraft preedit callback failed.", e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to invoke Minecraft's preedit callback.", e);
		}
	}
}
