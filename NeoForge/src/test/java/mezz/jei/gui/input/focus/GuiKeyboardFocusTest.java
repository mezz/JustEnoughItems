package mezz.jei.gui.input.focus;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GuiKeyboardFocusTest {
	@Test
	public void doesNotRestoreUnfocusedEditBoxInWidgets() {
		EditBox searchField = createEditBox();
		TestScreen screen = TestScreen.create(searchField);

		Runnable restoreFocus = GuiKeyboardFocus.suspendScreenFocus(screen);
		restoreFocus.run();
		Assertions.assertFalse(searchField.isFocused());
	}

	@Test
	public void restoresFocusedEditBoxInWidgets() {
		EditBox searchField = createEditBox();
		TestScreen screen = TestScreen.create(searchField);
		searchField.setCanLoseFocus(false);
		searchField.setFocused(true);

		Runnable restoreFocus = GuiKeyboardFocus.suspendScreenFocus(screen);

		Assertions.assertFalse(searchField.isFocused());
		Assertions.assertFalse(searchField.canLoseFocus);

		restoreFocus.run();

		Assertions.assertTrue(searchField.isFocused());
		Assertions.assertFalse(searchField.canLoseFocus);
		Assertions.assertNull(screen.getFocused());
	}

	@Test
	public void restoresFocusedEditBoxInNestedWidgets() {
		EditBox searchField = createEditBox();
		TestScreen nestedScreen = TestScreen.create(searchField);
		TestScreen screen = TestScreen.create(nestedScreen);
		searchField.setFocused(true);

		Runnable restoreFocus = GuiKeyboardFocus.suspendScreenFocus(screen);
		Assertions.assertFalse(searchField.isFocused());

		restoreFocus.run();
		Assertions.assertTrue(searchField.isFocused());
		Assertions.assertNull(screen.getFocused());
		Assertions.assertNull(nestedScreen.getFocused());
	}

	@Test
	public void restoresTheScreensFocusedWidget() {
		EditBox searchField = createEditBox();
		TestScreen screen = TestScreen.create(searchField);
		screen.setFocused(searchField);

		Runnable restoreFocus = GuiKeyboardFocus.suspendScreenFocus(screen);
		Assertions.assertNull(screen.getFocused());
		Assertions.assertFalse(searchField.isFocused());

		restoreFocus.run();
		Assertions.assertSame(searchField, screen.getFocused());
		Assertions.assertTrue(searchField.isFocused());
	}

	private static class TestScreen extends Screen {
		private List<GuiEventListener> widgets;

		private TestScreen() {
			super(Component.literal("test"));
		}

		private static TestScreen create(GuiEventListener... widgets) {
			TestScreen screen = new TestScreen();
			screen.widgets = List.of(widgets);
			return screen;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return widgets;
		}
	}

	private static EditBox createEditBox() {
		return new EditBox((Font) null, 0, 0, 100, 20, Component.literal("search"));
	}
}
