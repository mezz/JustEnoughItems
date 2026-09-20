package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.fabric.input.FabricAmecsSupport;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.test.client.TextInputTestUtil;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Verifies that complete GLFW key and character events are routed to JEI's search field.
 */
@SuppressWarnings("UnstableApiUsage")
public class JeiFabricTextInputClientGameTest implements FabricClientGameTest {
	private static final String FOCUS_SEARCH_KEY_MAPPING = "key.jei.focusSearch";
	private static final InputConstants.Key TEST_FOCUS_SEARCH_KEY = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F);

	@Override
	public void runTest(ClientGameTestContext context) {
		JUnitXmlTestReporter.runAndReportWithBooleanVariant(
			"fabric-client-gametest",
			"jei.fabric.disableAmecsSupport",
			"without-amecs",
			getClass().getSimpleName(),
			() -> assertTextInputRoutedToSearchField(context)
		);
	}

	private static void assertTextInputRoutedToSearchField(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(
				client -> Internal.getOptionalJeiRuntime().isPresent(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);

			context.runOnClient(client -> {
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
	}

	private static void focusSearchWithHotkey(KeyboardHandler keyboardHandler, long windowHandle) {
		focusSearchWithHotkey(keyboardHandler, windowHandle, () -> {});
	}

	private static void focusSearchWithHotkey(KeyboardHandler keyboardHandler, long windowHandle, Runnable assertions) {
		KeyMapping focusSearch = KeyMapping.get(FOCUS_SEARCH_KEY_MAPPING);
		if (focusSearch == null) {
			throw new AssertionError("Expected the focus-search key mapping to be registered.");
		}

		InputConstants.Key originalKey = KeyBindingHelper.getBoundKeyOf(focusSearch);
		Object originalModifiers = null;
		if (FabricAmecsSupport.isEnabled()) {
			originalModifiers = AmecsModifierState.clear(focusSearch);
		}
		try {
			// Modifier state cannot be synthesized by this callback-level test, so temporarily use an unmodified F.
			focusSearch.setKey(TEST_FOCUS_SEARCH_KEY);
			KeyMapping.resetMapping();
			KeyEvent event = new KeyEvent(GLFW.GLFW_KEY_F, 0, 0);
			TextInputTestUtil.invokeKeyPress(keyboardHandler, windowHandle, event);

			// A physical unmodified F also produces a character callback. JEI consumes the hotkey's character.
			TextInputTestUtil.invokeCharacterCallback(keyboardHandler, windowHandle, new CharacterEvent('f', 0));
			if (!Internal.getJeiRuntime().getIngredientFilter().getFilterText().isEmpty()) {
				throw new AssertionError("Expected the focus-search hotkey character to be consumed.");
			}
			assertions.run();
		} finally {
			focusSearch.setKey(originalKey);
			if (originalModifiers != null) {
				AmecsModifierState.restore(focusSearch, originalModifiers);
			}
			KeyMapping.resetMapping();
		}
	}

	/**
	 * Kept behind a nested class so the test can run when the optional AMECS classes are absent.
	 */
	private static final class AmecsModifierState {
		private AmecsModifierState() {

		}

		private static Object clear(KeyMapping keyMapping) {
			AmecsKeyModifierCombination boundModifiers = AmecsKeyModifiersApi.getBoundModifiers(keyMapping);
			AmecsKeyModifierCombination originalModifiers = new AmecsKeyModifierCombination();
			originalModifiers.copyFrom(boundModifiers);
			boundModifiers.unset();
			return originalModifiers;
		}

		private static void restore(KeyMapping keyMapping, Object originalModifiers) {
			AmecsKeyModifierCombination boundModifiers = AmecsKeyModifiersApi.getBoundModifiers(keyMapping);
			boundModifiers.copyFrom((AmecsKeyModifierCombination) originalModifiers);
		}
	}
}
