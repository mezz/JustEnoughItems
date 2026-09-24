package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.gui.GuiProperties;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import org.junit.jupiter.api.Test;

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
}
