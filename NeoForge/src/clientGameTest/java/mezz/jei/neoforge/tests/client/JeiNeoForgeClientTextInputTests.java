package mezz.jei.neoforge.tests.client;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.test.client.TextInputTestUtil;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Verifies that complete GLFW key and character events are routed to JEI's search field.
 */
public final class JeiNeoForgeClientTextInputTests {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String JUNIT_SUITE_NAME = "neoforge-client-gametest";
	private static final String FOCUS_SEARCH_KEY_MAPPING = "key.jei.focusSearch";
	private static final InputConstants.Key TEST_FOCUS_SEARCH_KEY = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F);

	private JeiNeoForgeClientTextInputTests() {

	}

	public static void run() {
		LOGGER.info("Starting JEI NeoForge client text input test");
		JUnitXmlTestReporter.runAndReport(
			JUNIT_SUITE_NAME,
			JeiNeoForgeClientTextInputTests.class.getSimpleName(),
			JeiNeoForgeClientTextInputTests::assertTextInputRoutedToSearchField
		);
		LOGGER.info("JEI NeoForge client text input test passed");
	}

	private static void assertTextInputRoutedToSearchField() {
		ClientTestUtil.runOnClient(client -> {
			if (client.player == null) {
				throw new AssertionError("Expected a client player for the text input test.");
			}
			InventoryScreen testScreen = new InventoryScreen(client.player);
			client.setScreen(testScreen);

			IngredientListOverlay ingredientListOverlay = (IngredientListOverlay) Internal.getJeiRuntime()
				.getIngredientListOverlay();
			if (!ingredientListOverlay.isListDisplayed()) {
				throw new AssertionError("Expected JEI's ingredient list to be displayed on the inventory screen.");
			}
			Internal.getJeiRuntime().getIngredientFilter().setFilterText("");

			ReflectionUtil reflectionUtil = new ReflectionUtil();
			GuiTextFieldFilter searchField = reflectionUtil.getFieldWithClass(ingredientListOverlay, GuiTextFieldFilter.class)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected JEI's ingredient overlay to contain a search field."));

			focusSearchWithHotkey(client.keyboardHandler, client.getWindow().handle());
			if (!ingredientListOverlay.hasKeyboardFocus()) {
				throw new AssertionError("Expected the focus-search hotkey to focus JEI's search field.");
			}

			try {
				searchField.setFocused(false);
				focusSearchWithHotkey(
					client.keyboardHandler, client.getWindow().handle(), () -> TextInputTestUtil.typePlainText(client.keyboardHandler, client.getWindow().handle(), searchField)
				);
			} finally {
				client.setScreen(null);
			}
		});
	}

	private static void focusSearchWithHotkey(KeyboardHandler keyboardHandler, long windowHandle) {
		focusSearchWithHotkey(keyboardHandler, windowHandle, () -> {});
	}

	private static void focusSearchWithHotkey(KeyboardHandler keyboardHandler, long windowHandle, Runnable assertions) {
		KeyMapping focusSearch = KeyMapping.get(FOCUS_SEARCH_KEY_MAPPING);
		if (focusSearch == null) {
			throw new AssertionError("Expected the focus-search key mapping to be registered.");
		}

		InputConstants.Key originalKey = focusSearch.getKey();
		KeyModifier originalModifier = focusSearch.getKeyModifier();
		try {
			// GLFW modifier state cannot be synthesized by this test harness, so temporarily remove Cmd/Ctrl.
			focusSearch.setKeyModifierAndCode(KeyModifier.NONE, TEST_FOCUS_SEARCH_KEY);
			KeyEvent event = new KeyEvent(GLFW.GLFW_KEY_F, 0, 0);
			TextInputTestUtil.invokeKeyPress(keyboardHandler, windowHandle, event);
			assertions.run();
		} finally {
			focusSearch.setKeyModifierAndCode(originalModifier, originalKey);
		}
	}
}
