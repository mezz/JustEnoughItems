package mezz.jei.neoforge.tests.client;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.test.client.ImeTextInputTestUtil;
import mezz.jei.test.client.PreeditBlockingContainerScreen;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Verifies that IME composition and committed text are routed to JEI's search field.
 */
public final class JeiNeoForgeClientTextInputTests {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String JUNIT_SUITE_NAME = "neoforge-client-gametest";
	private static final String FOCUS_SEARCH_KEY_MAPPING = "key.jei.focusSearch";
	private static final int KEY_CODE_F = InputConstants.KEY_F;
	private static final InputConstants.Key TEST_FOCUS_SEARCH_KEY = InputConstants.Type.KEYBOARD.getOrCreate(KEY_CODE_F);

	private JeiNeoForgeClientTextInputTests() {

	}

	public static void run() {
		LOGGER.info("Starting JEI NeoForge client text input test");
		JUnitXmlTestReporter.runAndReport(
			JUNIT_SUITE_NAME,
			JeiNeoForgeClientTextInputTests.class.getSimpleName(),
			JeiNeoForgeClientTextInputTests::assertImeInputRoutedToSearchField
		);
		LOGGER.info("JEI NeoForge client text input test passed");
	}

	private static void assertImeInputRoutedToSearchField() {
		ClientTestUtil.runOnClient(client -> {
			if (client.player == null) {
				throw new AssertionError("Expected a client player for the text input test.");
			}
			PreeditBlockingContainerScreen testScreen = new PreeditBlockingContainerScreen(client.player);
			client.gui.setScreen(testScreen);

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

			ImeTextInputTestUtil.assertContainerTextInputFocused(client, testScreen);
			searchField.setFocused(true);
			ImeTextInputTestUtil.assertSearchFieldTookTextInputFocus(client, testScreen, searchField);
			searchField.setFocused(false);
			testScreen.setFocused(null);

			CreativeModeInventoryScreen creativeScreen = ImeTextInputTestUtil.openCreativeSearchWithTextInputFocused(client, searchField);
			focusSearchWithHotkey(client.keyboardHandler, client.getWindow().handle());
			ImeTextInputTestUtil.assertSearchFieldTookCreativeSearchFocus(client, creativeScreen, searchField);
			ImeTextInputTestUtil.assertCreativeSearchFocusRestored(client, creativeScreen, searchField);

			client.gui.setScreen(testScreen);
			testScreen.setFocused(null);

			focusSearchWithHotkey(client.keyboardHandler, client.getWindow().handle());
			if (!ingredientListOverlay.hasKeyboardFocus()) {
				throw new AssertionError("Expected the focus-search hotkey to focus JEI's search field.");
			}

			try {
				if (client.gui.screen().getFocused() != searchField) {
					throw new AssertionError("Expected JEI's search field to own the screen focus.");
				}
				if (!(client.gui.screen() instanceof PreeditBlockingContainerScreen)) {
					throw new AssertionError("Expected this regression test to cover a screen that blocks normal preedit dispatch.");
				}

				searchField.setFocused(false);
				focusSearchWithHotkey(
					client.keyboardHandler, client.getWindow().handle(), () -> ImeTextInputTestUtil.typePlainText(client.keyboardHandler, client.getWindow().handle(), searchField)
				);
				ImeTextInputTestUtil.typeKoreanText(client.keyboardHandler, client.getWindow().handle(), searchField);
				ImeTextInputTestUtil.assertScreenCleanupKeepsChatTextInputEnabled(client, ingredientListOverlay, searchField);
				ImeTextInputTestUtil.assertRedundantUnfocusKeepsChatTextInputEnabled(client, searchField);
			} finally {
				client.gui.setScreen(null);
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
			// Native modifier state cannot be synthesized by this test harness, so temporarily remove Cmd/Ctrl.
			focusSearch.setKeyModifierAndCode(KeyModifier.NONE, TEST_FOCUS_SEARCH_KEY);
			KeyEvent event = new KeyEvent(KEY_CODE_F, 'f', 0);
			ImeTextInputTestUtil.invokeKeyPress(keyboardHandler, windowHandle, event);
			assertions.run();
		} finally {
			focusSearch.setKeyModifierAndCode(originalModifier, originalKey);
		}
	}
}
