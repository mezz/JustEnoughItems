package mezz.jei.test.client;

import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.util.GuiEventListenerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.GameType;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TextInputTestUtil {
	private TextInputTestUtil() {
	}

	public static void typePlainText(KeyboardHandler keyboardHandler, long windowHandle, GuiTextFieldFilter searchField) {
		// The focus-search key must also work as text when the search field already has focus.
		String text = "flint";
		int[] keys = {GLFW.GLFW_KEY_F, GLFW.GLFW_KEY_L, GLFW.GLFW_KEY_I, GLFW.GLFW_KEY_N, GLFW.GLFW_KEY_T};
		for (int i = 0; i < keys.length; i++) {
			char character = text.charAt(i);
			int event = keys[i];
			try {
				invokeKeyPress(keyboardHandler, windowHandle, event);
				if (!searchField.isFocused()) {
					throw new AssertionError("Expected typing '" + character + "' to keep JEI's search field focused.");
				}
				invokeCharacterCallback(keyboardHandler, windowHandle, character);
				assertSearchText(searchField, text.substring(0, i + 1));
			} finally {
				invokeKeyEvent(keyboardHandler, windowHandle, GLFW.GLFW_RELEASE, event);
			}
		}
		searchField.setValue("");
	}

	public static void invokeKeyPress(KeyboardHandler keyboardHandler, long windowHandle, int key) {
		invokeKeyEvent(keyboardHandler, windowHandle, GLFW.GLFW_PRESS, key);
	}

	public static void invokeKeyRelease(KeyboardHandler keyboardHandler, long windowHandle, int key) {
		invokeKeyEvent(keyboardHandler, windowHandle, GLFW.GLFW_RELEASE, key);
	}

	public static void typeImeText(KeyboardHandler keyboardHandler, long windowHandle, GuiTextFieldFilter searchField) {
		String text = "苹果";
		String expected = searchField.getValue() + text;
		// An IME can commit multiple characters without sending a corresponding key press.
		text.codePoints().forEach(codePoint -> invokeCharacterCallback(keyboardHandler, windowHandle, codePoint));
		if (!searchField.isFocused()) {
			throw new AssertionError("Expected committing IME text to keep JEI's search field focused.");
		}
		assertSearchText(searchField, expected);
	}

	public static void assertImeInputAfterBackspace(KeyboardHandler keyboardHandler, long windowHandle, GuiTextFieldFilter searchField) {
		searchField.setValue("苹果");
		for (int attempt = 0; attempt < 2; attempt++) {
			for (int i = 0; i < 2; i++) {
				invokeKeyPress(keyboardHandler, windowHandle, GLFW.GLFW_KEY_BACKSPACE);
				invokeKeyRelease(keyboardHandler, windowHandle, GLFW.GLFW_KEY_BACKSPACE);
			}
			assertSearchText(searchField, "");
			typeImeText(keyboardHandler, windowHandle, searchField);
		}
		searchField.setValue("");
	}

	private static void invokeKeyEvent(KeyboardHandler keyboardHandler, long windowHandle, int action, int key) {
		try {
			Method method = KeyboardHandler.class.getDeclaredMethod("keyPress", long.class, int.class, int.class, int.class, int.class);
			method.setAccessible(true);
			method.invoke(keyboardHandler, windowHandle, key, 0, action, 0);
		} catch (InvocationTargetException e) {
			throw new AssertionError("The Minecraft key callback failed.", e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to invoke Minecraft's key callback.", e);
		}
	}

	public static void invokeCharacterCallback(KeyboardHandler keyboardHandler, long windowHandle, int character) {
		try {
			Method method = KeyboardHandler.class.getDeclaredMethod("charTyped", long.class, int.class, int.class);
			method.setAccessible(true);
			method.invoke(keyboardHandler, windowHandle, character, 0);
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

	public static CreativeModeInventoryScreen openCreativeSearchWithTextInputFocused(
		Minecraft minecraft,
		GuiTextFieldFilter searchField
	) {
		if (minecraft.player == null) {
			throw new AssertionError("Expected a client player for the creative search test.");
		}

		searchField.clearFocus();
		GameType originalGameType = minecraft.gameMode.getPlayerMode();
		CreativeModeInventoryScreen screen;
		try {
			minecraft.gameMode.setLocalMode(GameType.CREATIVE);
			screen = new CreativeModeInventoryScreen(
				minecraft.player,
				minecraft.player.connection.enabledFeatures(),
				minecraft.options.operatorItemsTab().get()
			);
			minecraft.setScreen(screen);
			selectCreativeSearchTab(screen);
		} finally {
			minecraft.gameMode.setLocalMode(originalGameType);
		}

		EditBox creativeSearch = getCreativeSearch(screen);
		if (!creativeSearch.isFocused() || !creativeSearch.isVisible()) {
			throw new AssertionError("Expected the creative inventory search box to start focused and visible.");
		}
		if (searchField.isFocused()) {
			throw new AssertionError("Expected JEI's search field to start unfocused in the creative search tab.");
		}

		return screen;
	}

	public static void assertCreativeSearchFocusRestored(
		CreativeModeInventoryScreen screen,
		GuiTextFieldFilter searchField
	) {
		searchField.clearFocus();
		if (!getCreativeSearch(screen).isFocused()) {
			throw new AssertionError("Expected unfocusing JEI to restore the creative inventory search box.");
		}
	}

	private static void selectCreativeSearchTab(CreativeModeInventoryScreen screen) {
		try {
			Method method = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
			method.setAccessible(true);
			method.invoke(screen, CreativeModeTabs.searchTab());
		} catch (InvocationTargetException e) {
			throw new AssertionError("Selecting the creative search tab failed.", e.getCause());
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to select the creative search tab.", e);
		}
	}

	private static EditBox getCreativeSearch(CreativeModeInventoryScreen screen) {
		return GuiEventListenerUtil.getEditBoxes(screen)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected the creative search tab to contain an input field."));
	}
}
