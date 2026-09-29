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
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

/**
 * Verifies search focus and text editing through Minecraft's input callbacks.
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
					assertEditorPriority(client, searchField);
					TextInputTestUtil.invokeKeyPress(client.keyboardHandler, client.getWindow().handle(), new KeyEvent(InputConstants.KEY_LSHIFT, 0, 0));
					if (!searchField.isFocused()) {
						throw new AssertionError("An unhandled modifier key must preserve search focus.");
					}

					searchField.setFocused(false);
					focusSearchWithHotkey(
						client.keyboardHandler, client.getWindow().handle(), () -> TextInputTestUtil.typePlainText(client.keyboardHandler, client.getWindow().handle(), searchField)
					);
				} finally {
					client.setScreen(null);
				}
			});
			assertMouseFocusChangesOnRelease(context);
		}
	}

	private static void assertMouseFocusChangesOnRelease(ClientGameTestContext context) {
		GuiTextFieldFilter searchField = context.computeOnClient(client -> {
			client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			var overlay = (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
			return new ReflectionUtil().getFieldWithClass(overlay, GuiTextFieldFilter.class)
				.findFirst()
				.orElseThrow(() -> new AssertionError("Expected the ingredient overlay to contain a search field."));
		});
		String originalText = context.computeOnClient(client -> searchField.getValue());
		try {
			context.waitTick();
			context.runOnClient(client -> {
				searchField.setValue("stone");
				searchField.moveCursorToEnd(false);
			});
			int originalCursor = context.computeOnClient(client -> searchField.getCursorPosition());
			double[] fieldPosition = context.computeOnClient(client -> new double[]{searchField.getX() + 1, searchField.getY() + 1});
			moveMouse(context, fieldPosition[0], fieldPosition[1]);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.waitTick();
			context.runOnClient(client -> {
				if (searchField.isFocused() || searchField.getCursorPosition() != originalCursor) {
					throw new AssertionError("Pressing the mouse button must not change search focus or move its text cursor.");
				}
			});
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.waitTick();
			context.runOnClient(client -> {
				if (!searchField.isFocused()) {
					throw new AssertionError("Releasing over the search field must focus it.");
				}
				if (searchField.getCursorPosition() == originalCursor) {
					throw new AssertionError("Releasing over the search field must move the text cursor to the click position.");
				}
			});

			moveMouse(context, 1, 1);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.waitTick();
			context.runOnClient(client -> {
				if (!searchField.isFocused()) {
					throw new AssertionError("Pressing outside the search field must keep its focus until release.");
				}
			});
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.waitTick();
			context.runOnClient(client -> {
				if (searchField.isFocused()) {
					throw new AssertionError("Releasing outside the search field must clear its focus.");
				}
			});

			moveMouse(context, fieldPosition[0], fieldPosition[1]);
			context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
			moveMouse(context, 1, 1);
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.waitTick();
			context.runOnClient(client -> {
				if (searchField.isFocused()) {
					throw new AssertionError("A search click canceled by releasing outside must not focus the field.");
				}
			});
		} finally {
			context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
			context.runOnClient(client -> {
				searchField.setValue(originalText);
				client.setScreen(null);
			});
		}
	}

	private static void moveMouse(ClientGameTestContext context, double x, double y) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(x * scale, y * scale);
		context.waitTick();
	}

	private static void assertEditorPriority(Minecraft client, GuiTextFieldFilter searchField) {
		KeyMapping editMode = Objects.requireNonNull(KeyMapping.get("key.jei.toggleEditMode"));
		var originalKey = KeyBindingHelper.getBoundKeyOf(editMode);
		var toggles = Internal.getClientToggleState();
		boolean originalEditMode = toggles.isEditModeEnabled();
		String originalText = searchField.getValue();
		try {
			toggles.setEditModeEnabled(false);
			editMode.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_BACKSPACE));
			searchField.setValue("stone");
			var backspace = new KeyEvent(InputConstants.KEY_BACKSPACE, 0, 0);
			TextInputTestUtil.invokeKeyPress(client.keyboardHandler, client.getWindow().handle(), backspace);
			if (!searchField.getValue().equals("ston") || toggles.isEditModeEnabled()) {
				throw new AssertionError("Focused search editing must take precedence over a conflicting global shortcut.");
			}

			searchField.setFocused(false);
			TextInputTestUtil.invokeKeyPress(client.keyboardHandler, client.getWindow().handle(), backspace);
			if (!toggles.isEditModeEnabled() || !searchField.getValue().equals("ston")) {
				throw new AssertionError("The same shortcut must execute immediately once the search field loses focus.");
			}
		} finally {
			editMode.setKey(originalKey);
			toggles.setEditModeEnabled(originalEditMode);
			searchField.setValue(originalText);
			searchField.setFocused(true);
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

			// A physical unmodified F also produces a character callback. JEI handles the shortcut's character.
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
