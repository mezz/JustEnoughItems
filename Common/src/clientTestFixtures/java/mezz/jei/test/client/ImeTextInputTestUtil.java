package mezz.jei.test.client;

import com.mojang.blaze3d.platform.TextInputManager;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.IMEPreeditOverlay;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.GameType;
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

	public static void assertRedundantUnfocusKeepsChatTextInputEnabled(
		Minecraft minecraft,
		GuiTextFieldFilter searchField
	) {
		ChatTextInputFixture fixture = openChatWithTextInputFocused(minecraft, searchField);

		searchField.setFocused(false);

		if (fixture.screen().getFocused() != fixture.chatInput()) {
			throw new AssertionError("Expected a redundant JEI unfocus call to preserve the chat input's screen focus.");
		}
		if (!fixture.chatInput().isFocused()) {
			throw new AssertionError("Expected a redundant JEI unfocus call to leave the chat input focused.");
		}
		assertTextInputEnabled(minecraft, "a redundant JEI unfocus call");
	}

	public static void assertScreenCleanupKeepsChatTextInputEnabled(
		Minecraft minecraft,
		IngredientListOverlay ingredientListOverlay,
		GuiTextFieldFilter searchField
	) {
		ChatTextInputFixture fixture = openChatWithTextInputFocused(minecraft, searchField);

		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(fixture.screen())
			.forceUpdate();

		assertChatInputFocused(fixture, "JEI clearing its hidden overlay state");
		assertTextInputEnabled(minecraft, "JEI clearing its hidden overlay state");
	}

	public static void assertContainerTextInputFocused(
		Minecraft minecraft,
		PreeditBlockingContainerScreen screen
	) {
		if (screen.getFocused() != screen.getTextInput() || !screen.getTextInput().isFocused()) {
			throw new AssertionError("Expected the container's text input to start focused.");
		}
		assertTextInputEnabled(minecraft, "the container's text input gaining focus");
	}

	public static void assertSearchFieldTookTextInputFocus(
		Minecraft minecraft,
		PreeditBlockingContainerScreen screen,
		GuiTextFieldFilter searchField
	) {
		if (screen.getFocused() != searchField || !searchField.isFocused()) {
			throw new AssertionError("Expected JEI's search field to take focus on the container screen.");
		}
		if (screen.getTextInput().isFocused()) {
			throw new AssertionError("Expected JEI's search field to release the container's text input.");
		}
		assertTextInputEnabled(minecraft, "JEI's search field taking focus on a container screen");
	}

	public static CreativeModeInventoryScreen openCreativeSearchWithTextInputFocused(
		Minecraft minecraft,
		GuiTextFieldFilter searchField
	) {
		if (minecraft.player == null) {
			throw new AssertionError("Expected a client player for the creative search test.");
		}

		searchField.setFocused(false);
		GameType originalGameType = minecraft.gameMode.getPlayerMode();
		CreativeModeInventoryScreen screen;
		try {
			minecraft.gameMode.setLocalMode(GameType.CREATIVE);
			screen = new CreativeModeInventoryScreen(
				minecraft.player,
				minecraft.player.connection.enabledFeatures(),
				minecraft.options.operatorItemsTab().get()
			);
			minecraft.gui.setScreen(screen);
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
		assertTextInputEnabled(minecraft, "the creative inventory search box gaining focus");
		return screen;
	}

	public static void assertSearchFieldTookCreativeSearchFocus(
		Minecraft minecraft,
		CreativeModeInventoryScreen screen,
		GuiTextFieldFilter searchField
	) {
		if (screen.getFocused() != searchField || !searchField.isFocused()) {
			throw new AssertionError("Expected the focus-search hotkey to focus JEI from the creative search tab.");
		}
		if (getCreativeSearch(screen).isFocused()) {
			throw new AssertionError("Expected JEI to release the creative inventory search box.");
		}
		assertTextInputEnabled(minecraft, "JEI taking focus from the creative inventory search box");
	}

	public static void assertCreativeSearchFocusRestored(
		Minecraft minecraft,
		CreativeModeInventoryScreen screen,
		GuiTextFieldFilter searchField
	) {
		searchField.setFocused(false);
		if (!getCreativeSearch(screen).isFocused()) {
			throw new AssertionError("Expected unfocusing JEI to restore the creative inventory search box.");
		}
		assertTextInputEnabled(minecraft, "restoring the creative inventory search box");
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
		ReflectionUtil reflectionUtil = new ReflectionUtil();
		return reflectionUtil.getFieldWithClass(screen, EditBox.class)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected the creative search tab to contain an input field."));
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

	private static ChatTextInputFixture openChatWithTextInputFocused(
		Minecraft minecraft,
		GuiTextFieldFilter searchField
	) {
		ChatScreen chatScreen = new ChatScreen("", false);
		minecraft.gui.setScreen(chatScreen);

		ReflectionUtil reflectionUtil = new ReflectionUtil();
		EditBox chatInput = reflectionUtil.getFieldWithClass(chatScreen, EditBox.class)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Expected the chat screen to contain an input field."));

		searchField.setFocused(false);
		chatScreen.setFocused(chatInput);
		chatInput.setFocused(true);

		if (searchField.isFocused()) {
			throw new AssertionError("Expected JEI's search field to start unfocused.");
		}
		ChatTextInputFixture fixture = new ChatTextInputFixture(chatScreen, chatInput);
		assertChatInputFocused(fixture, "the chat screen opening");
		assertTextInputEnabled(minecraft, "the chat input gaining focus");
		return fixture;
	}

	private static void assertChatInputFocused(ChatTextInputFixture fixture, String operation) {
		if (fixture.screen().getFocused() != fixture.chatInput() || !fixture.chatInput().isFocused()) {
			throw new AssertionError("Expected the chat input to remain focused after " + operation + ".");
		}
	}

	private static void assertTextInputEnabled(Minecraft minecraft, String operation) {
		try {
			TextInputManager textInputManager = minecraft.textInputManager();
			var field = TextInputManager.class.getDeclaredField("textInputEnabled");
			field.setAccessible(true);
			if (!field.getBoolean(textInputManager)) {
				throw new AssertionError("Expected text input to remain enabled after " + operation + ".");
			}
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to inspect Minecraft's text-input state.", e);
		}
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

	private record ChatTextInputFixture(ChatScreen screen, EditBox chatInput) {

	}
}
