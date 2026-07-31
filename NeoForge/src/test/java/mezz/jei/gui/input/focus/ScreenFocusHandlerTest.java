package mezz.jei.gui.input.focus;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ScreenFocusHandlerTest {
	@Test
	public void doesNotRestoreUnfocusedEditBoxDiscoveredByReflection() {
		TestScreen screen = TestScreen.create();

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);

		Assertions.assertNull(focusHandler);
		Assertions.assertFalse(screen.searchField.isFocused());
	}

	@Test
	public void restoresFocusedEditBoxDiscoveredByReflection() {
		TestScreen screen = TestScreen.create();
		screen.searchField.setCanLoseFocus(false);
		screen.searchField.setFocused(true);

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);
		Assertions.assertNotNull(focusHandler);

		focusHandler.unFocus();

		Assertions.assertFalse(screen.searchField.isFocused());
		Assertions.assertTrue(screen.searchField.canLoseFocus);

		focusHandler.focus();

		Assertions.assertTrue(screen.searchField.isFocused());
		Assertions.assertFalse(screen.searchField.canLoseFocus);
	}

	private static class TestScreen extends Screen {
		private TestEditBox searchField;

		private TestScreen() {
			super(Component.literal("test"));
		}

		private static TestScreen create() {
			TestScreen screen = allocateInstance(TestScreen.class);
			screen.searchField = new TestEditBox();
			return screen;
		}
	}

	private static class TestEditBox extends EditBox {
		private boolean focused;

		private TestEditBox() {
			super((Font) null, 0, 0, 100, 20, Component.literal("search"));
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
		}

		@Override
		public boolean isFocused() {
			return this.focused;
		}
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
