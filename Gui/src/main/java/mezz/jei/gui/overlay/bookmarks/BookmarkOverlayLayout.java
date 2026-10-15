package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.util.ImmutableRect2i;

import java.util.Set;

final class BookmarkOverlayLayout {
	private BookmarkOverlayLayout() {
	}

	static ImmutableRect2i calculateDisplayArea(
		IGuiProperties guiProperties,
		Set<ImmutableRect2i> guiExclusionAreas
	) {
		int width = Math.max(0, guiProperties.getGuiLeft());
		ImmutableRect2i displayArea = new ImmutableRect2i(0, 0, width, guiProperties.getScreenHeight());
		int displayRight = displayArea.x() + displayArea.width();
		int availableRight = displayRight;
		for (ImmutableRect2i exclusionArea : guiExclusionAreas) {
			boolean overlapsVertically = exclusionArea.y() < (guiProperties.getGuiTop() + guiProperties.getGuiYSize()) &&
				exclusionArea.y() + exclusionArea.height() > guiProperties.getGuiTop();
			// Screen-edge sidebars leave the full width available for slots above or below them.
			boolean attachedToGuiLeft = exclusionArea.x() > displayArea.x() &&
				exclusionArea.x() < displayRight &&
				exclusionArea.x() + exclusionArea.width() >= displayRight;
			if (overlapsVertically && attachedToGuiLeft) {
				availableRight = Math.min(availableRight, Math.max(displayArea.x(), exclusionArea.x()));
			}
		}
		return displayArea.cropRight(displayRight - availableRight);
	}
}
