package mezz.jei.gui.input.focus;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ScreenFocusHandlerTest {
	@Test
	public void doesNotRestoreUnfocusedEditBoxInWidgets() {
		EditBox searchField = createEditBox();
		TestScreen screen = TestScreen.create(searchField);

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);

		Assertions.assertNull(focusHandler);
		Assertions.assertFalse(searchField.isFocused());
	}

	@Test
	public void restoresFocusedEditBoxInWidgets() {
		EditBox searchField = createEditBox();
		TestScreen screen = TestScreen.create(searchField);
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
		TestScreen nestedScreen = TestScreen.create(searchField);
		TestScreen screen = TestScreen.create(nestedScreen);
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
		private List<GuiEventListener> widgets;

		private TestScreen() {
			super(Component.literal("test"));
		}

		private static TestScreen create(GuiEventListener... widgets) {
			// This Minecraft version's constructor needs a running client.
			TestScreen screen = allocateInstance(TestScreen.class);
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
	private static <T> T allocateInstance(Class<T> instanceClass) {
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Object unsafe = theUnsafe.get(null);
			Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
			Object instance = allocateInstance.invoke(unsafe, instanceClass);
			return instanceClass.cast(instance);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to allocate test instance for " + instanceClass.getName(), e);
		}
	}
}
