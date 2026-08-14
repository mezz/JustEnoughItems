package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifierCombination;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.fabric.input.FabricAmecsSupport;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.test.client.ImeTextInputTestUtil;
import mezz.jei.test.client.PreeditBlockingContainerScreen;
import mezz.jei.test.lib.JUnitXmlTestReporter;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Verifies that IME composition and committed text are routed to JEI's search field.
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
			() -> assertImeInputRoutedToSearchField(context)
		);
	}

	private static void assertImeInputRoutedToSearchField(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(
				client -> Internal.getOptionalJeiRuntime().isPresent(),
				ClientGameTestContext.DEFAULT_TIMEOUT
			);

			context.runOnClient(client -> {
				if (client.player == null) {
					throw new AssertionError("Expected a client player for the text input test.");
				}
				client.gui.setScreen(new PreeditBlockingContainerScreen(client.player));

				IngredientListOverlay ingredientListOverlay = (IngredientListOverlay) Internal.getJeiRuntime()
					.getIngredientListOverlay();
				if (!ingredientListOverlay.isListDisplayed()) {
					throw new AssertionError("Expected JEI's ingredient list to be displayed on the inventory screen.");
				}
				Internal.getJeiRuntime().getIngredientFilter().setFilterText("");
				focusSearchWithHotkey(client.keyboardHandler, client.getWindow().handle());
				if (!ingredientListOverlay.hasKeyboardFocus()) {
					throw new AssertionError("Expected the focus-search hotkey to focus JEI's search field.");
				}

				ReflectionUtil reflectionUtil = new ReflectionUtil();
				GuiTextFieldFilter searchField = reflectionUtil.getFieldWithClass(ingredientListOverlay, GuiTextFieldFilter.class)
					.findFirst()
					.orElseThrow(() -> new AssertionError("Expected JEI's ingredient overlay to contain a search field."));
				try {
					if (client.gui.screen().getFocused() != searchField) {
						throw new AssertionError("Expected JEI's search field to own the screen focus.");
					}
					if (!(client.gui.screen() instanceof PreeditBlockingContainerScreen)) {
						throw new AssertionError("Expected this regression test to cover a screen that blocks normal preedit dispatch.");
					}

					ImeTextInputTestUtil.typeKoreanText(client.keyboardHandler, client.getWindow().handle(), searchField);
				} finally {
					client.gui.setScreen(null);
				}
			});
		}
	}

	private static void focusSearchWithHotkey(KeyboardHandler keyboardHandler, long windowHandle) {
		KeyMapping focusSearch = KeyMapping.get(FOCUS_SEARCH_KEY_MAPPING);
		if (focusSearch == null) {
			throw new AssertionError("Expected the focus-search key mapping to be registered.");
		}

		InputConstants.Key originalKey = KeyMappingHelper.getBoundKeyOf(focusSearch);
		Object originalModifiers = null;
		if (FabricAmecsSupport.isEnabled()) {
			originalModifiers = AmecsModifierState.clear(focusSearch);
		}
		try {
			// Modifier state cannot be synthesized by this callback-level test, so temporarily use an unmodified F.
			focusSearch.setKey(TEST_FOCUS_SEARCH_KEY);
			KeyMapping.resetMapping();
			KeyEvent event = new KeyEvent(GLFW.GLFW_KEY_F, 0, 0);
			ImeTextInputTestUtil.invokeKeyPress(keyboardHandler, windowHandle, event);

			// A physical unmodified F also produces a character callback. JEI consumes the hotkey's character.
			ImeTextInputTestUtil.invokeCharacterCallback(keyboardHandler, windowHandle, new CharacterEvent('f'));
			if (!Internal.getJeiRuntime().getIngredientFilter().getFilterText().isEmpty()) {
				throw new AssertionError("Expected the focus-search hotkey character to be consumed.");
			}
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
