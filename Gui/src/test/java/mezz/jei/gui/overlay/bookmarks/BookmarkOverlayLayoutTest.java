package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.gui.GuiProperties;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BookmarkOverlayLayoutTest {
	@Test
	public void attachedGuiTabsReduceBookmarkDisplayArea() {
		IGuiProperties guiProperties = new GuiProperties(Screen.class, 200, 20, 100, 120, 400, 300);
		ImmutableRect2i tabsArea = new ImmutableRect2i(167, 24, 36, 250);

		ImmutableRect2i displayArea = BookmarkOverlayLayout.calculateDisplayArea(
			guiProperties,
			Set.of(tabsArea)
		);

		assertEquals(new ImmutableRect2i(0, 0, tabsArea.x(), guiProperties.screenHeight()), displayArea);
		assertFalse(displayArea.intersects(tabsArea));
	}

	@Test
	public void separateExclusionRemainsInsideBookmarkDisplayArea() {
		IGuiProperties guiProperties = new GuiProperties(Screen.class, 200, 20, 100, 120, 400, 300);
		ImmutableRect2i separateExclusion = new ImmutableRect2i(150, 24, 20, 100);

		ImmutableRect2i displayArea = BookmarkOverlayLayout.calculateDisplayArea(
			guiProperties,
			Set.of(separateExclusion)
		);

		assertEquals(new ImmutableRect2i(0, 0, guiProperties.guiLeft(), guiProperties.screenHeight()), displayArea);
	}

	@ParameterizedTest
	@CsvSource({"0, 17", "221, 17", "0, 51", "187, 51"})
	public void screenCornerExclusionKeepsBookmarkDisplayArea(int y, int height) {
		IGuiProperties guiProperties = new GuiProperties(Screen.class, 72, 37, 176, 166, 320, 240);
		ImmutableRect2i sidebarArea = new ImmutableRect2i(0, y, 85, height);

		ImmutableRect2i displayArea = BookmarkOverlayLayout.calculateDisplayArea(
			guiProperties,
			Set.of(sidebarArea)
		);

		assertEquals(new ImmutableRect2i(0, 0, 72, 240), displayArea);
	}

	@ParameterizedTest
	@CsvSource({"0, 37", "203, 37"})
	public void exclusionAboveOrBelowGuiKeepsBookmarkDisplayArea(int y, int height) {
		IGuiProperties guiProperties = new GuiProperties(Screen.class, 72, 37, 176, 166, 320, 240);
		ImmutableRect2i exclusionArea = new ImmutableRect2i(40, y, 35, height);

		ImmutableRect2i displayArea = BookmarkOverlayLayout.calculateDisplayArea(
			guiProperties,
			Set.of(exclusionArea)
		);

		assertEquals(new ImmutableRect2i(0, 0, 72, 240), displayArea);
	}

	@Test
	public void screenCornerExclusionPreservesAttachedGuiTabReservation() {
		IGuiProperties guiProperties = new GuiProperties(Screen.class, 200, 20, 100, 120, 400, 300);
		ImmutableRect2i tabsArea = new ImmutableRect2i(167, 24, 36, 250);
		ImmutableRect2i sidebarArea = new ImmutableRect2i(0, 0, 204, 17);

		ImmutableRect2i displayArea = BookmarkOverlayLayout.calculateDisplayArea(
			guiProperties,
			Set.of(tabsArea, sidebarArea)
		);

		assertEquals(new ImmutableRect2i(0, 0, tabsArea.x(), guiProperties.screenHeight()), displayArea);
		assertFalse(displayArea.intersects(tabsArea));
	}
}
