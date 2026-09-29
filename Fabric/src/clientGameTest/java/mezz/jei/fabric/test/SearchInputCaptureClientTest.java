package mezz.jei.fabric.test;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ReflectionUtil;
import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.test.client.TextInputTestUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.Objects;

/** Checks editing priority and focus changes through native key and mouse callbacks. */
final class SearchInputCaptureClientTest {
	static void run() {
		var context = new FabricClientTestContext();
		context.runOnClient(client -> {
			client.setScreen(new InventoryScreen(Objects.requireNonNull(client.player)));
			var overlay = (IngredientListOverlay) Internal.getJeiRuntime().getIngredientListOverlay();
			var field = new ReflectionUtil().getFieldWithClass(overlay, GuiTextFieldFilter.class).findFirst().orElseThrow();
			field.setFocused(true);
			assertEditorPriority(client, field);
			TextInputTestUtil.invokeKeyPress(client.keyboardHandler, client.getWindow().getWindow(), InputConstants.KEY_LSHIFT);
			if (!field.isFocused()) {
				throw new AssertionError("An unhandled modifier key must preserve search focus.");
			}
			field.setFocused(false);
		});
		assertMouseFocusChangesOnRelease(context);
	}
	private static void assertMouseFocusChangesOnRelease(FabricClientTestContext context) {
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

	private static void moveMouse(FabricClientTestContext context, double x, double y) {
		double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
		context.getInput().setCursorPos(x * scale, y * scale);
		context.waitTick();
	}

	private static void assertEditorPriority(Minecraft client, GuiTextFieldFilter searchField) {
		KeyMapping editMode = java.util.Arrays.stream(client.options.keyMappings).filter(mapping -> mapping.getName().equals("key.jei.toggleEditMode")).findFirst().orElseThrow();
		var originalKey = KeyBindingHelper.getBoundKeyOf(editMode);
		var toggles = Internal.getClientToggleState();
		boolean originalEditMode = toggles.isEditModeEnabled();
		String originalText = searchField.getValue();
		try {
			toggles.setEditModeEnabled(false);
			editMode.setKey(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_BACKSPACE));
			searchField.setValue("stone");
			int backspace = InputConstants.KEY_BACKSPACE;
			TextInputTestUtil.invokeKeyPress(client.keyboardHandler, client.getWindow().getWindow(), backspace);
			if (!searchField.getValue().equals("ston") || toggles.isEditModeEnabled()) {
				throw new AssertionError("Focused search editing must take precedence over a conflicting global shortcut.");
			}

			searchField.setFocused(false);
			TextInputTestUtil.invokeKeyPress(client.keyboardHandler, client.getWindow().getWindow(), backspace);
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

}
