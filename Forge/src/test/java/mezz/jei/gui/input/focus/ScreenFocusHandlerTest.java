package mezz.jei.gui.input.focus;

import mezz.jei.test.lib.ForgeTestBootstrap;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ScreenFocusHandlerTest {
	@BeforeAll
	public static void setup() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		ForgeTestBootstrap.bootStrap();
	}

	@Test
	public void doesNotRestoreUnfocusedEditBoxDiscoveredByReflection() {
		TestScreen screen = new TestScreen();

		ScreenFocusHandler focusHandler = ScreenFocusHandler.create(screen);

		Assertions.assertNull(focusHandler);
		Assertions.assertFalse(screen.searchField.isFocused());
	}

	@Test
	public void restoresFocusedEditBoxDiscoveredByReflection() {
		TestScreen screen = new TestScreen();
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
		private final EditBox searchField = new EditBox((Font) null, 0, 0, 100, 20, Component.literal("search"));

		private TestScreen() {
			super(Component.literal("test"));
		}
	}
}
