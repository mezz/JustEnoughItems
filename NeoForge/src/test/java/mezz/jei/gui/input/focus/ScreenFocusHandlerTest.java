package mezz.jei.gui.input.focus;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ScreenFocusHandlerTest {
	@Test
	public void doesNotRestoreUnfocusedEditBoxInWidgets() {
		EditBox searchField = createEditBox();
		TestScreen screen = new TestScreen(searchField);

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);

		Assertions.assertNull(focusHandler);
		Assertions.assertFalse(searchField.isFocused());
	}

	@Test
	public void restoresFocusedEditBoxInWidgets() {
		EditBox searchField = createEditBox();
		TestScreen screen = new TestScreen(searchField);
		searchField.setCanLoseFocus(false);
		searchField.setFocused(true);

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);
		Assertions.assertNotNull(focusHandler);

		focusHandler.unFocus();

		Assertions.assertFalse(searchField.isFocused());
		Assertions.assertTrue(searchField.canLoseFocus);

		focusHandler.focus();

		Assertions.assertTrue(searchField.isFocused());
		Assertions.assertFalse(searchField.canLoseFocus);
		Assertions.assertNull(screen.getFocused());
	}

	@Test
	public void restoresFocusedEditBoxInNestedWidgets() {
		EditBox searchField = createEditBox();
		TestScreen nestedScreen = new TestScreen(searchField);
		TestScreen screen = new TestScreen() {
			@Override
			public List<? extends GuiEventListener> children() {
				return List.of(nestedScreen);
			}
		};
		searchField.setFocused(true);

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);
		Assertions.assertNotNull(focusHandler);

		focusHandler.unFocus();
		Assertions.assertFalse(searchField.isFocused());

		focusHandler.focus();
		Assertions.assertTrue(searchField.isFocused());
		Assertions.assertNull(screen.getFocused());
		Assertions.assertNull(nestedScreen.getFocused());
	}

	private static class TestScreen extends Screen {
		private TestScreen(EditBox... editBoxes) {
			super(null, null, Component.literal("test"));
			for (EditBox editBox : editBoxes) {
				addWidget(editBox);
			}
		}
	}

	private static EditBox createEditBox() {
		EditBox editBox = new EditBox((Font) null, 0, 0, 100, 20, Component.literal("search"));
		// Disable text input callbacks so focus can be tested without a running client.
		editBox.setEditable(false);
		return editBox;
	}
}
