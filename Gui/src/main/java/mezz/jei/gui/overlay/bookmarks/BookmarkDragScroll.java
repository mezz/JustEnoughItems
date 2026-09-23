package mezz.jei.gui.overlay.bookmarks;

import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.overlay.ingredients.IngredientGridLayout;

import java.util.function.LongSupplier;

/** Scrolls near the top and bottom edges, accumulating whole rows for non-smooth scrolling. */
final class BookmarkDragScroll {
	private static final int EDGE_SIZE = 20;
	private static final double PIXELS_PER_SECOND = 120;
	private final LongSupplier nanoTime;
	private long lastUpdateTime;
	private double pendingPixels;

	BookmarkDragScroll(LongSupplier nanoTime) {
		this.nanoTime = nanoTime;
		this.lastUpdateTime = nanoTime.getAsLong();
	}

	double update(ImmutableRect2i area, boolean usesScrollbar, boolean smoothScrolling, double mouseX, double mouseY) {
		long now = this.nanoTime.getAsLong();
		// Limit catch-up after a stalled frame so the list cannot jump past the intended drop target.
		double elapsedSeconds = Math.clamp((now - this.lastUpdateTime) / 1_000_000_000.0, 0, 0.05);
		this.lastUpdateTime = now;
		double speed = getScrollSpeed(area, usesScrollbar, mouseX, mouseY);
		if (speed == 0 || Math.signum(speed) != Math.signum(this.pendingPixels)) {
			this.pendingPixels = 0;
		}
		double pixels = speed * elapsedSeconds;
		if (smoothScrolling) {
			this.pendingPixels = 0;
			return pixels;
		}
		this.pendingPixels += pixels;
		int rows = (int) (this.pendingPixels / IngredientGridLayout.INGREDIENT_HEIGHT);
		int rowPixels = rows * IngredientGridLayout.INGREDIENT_HEIGHT;
		this.pendingPixels -= rowPixels;
		return rowPixels;
	}

	private static double getScrollSpeed(ImmutableRect2i area, boolean usesScrollbar, double mouseX, double mouseY) {
		if (!usesScrollbar || area.isEmpty() || mouseX < area.x() || mouseX >= area.x() + area.width()) {
			return 0;
		}
		double edgeSize = Math.min(EDGE_SIZE, area.height() / 2.0);
		double topEdge = area.y() + edgeSize;
		double bottomEdge = area.y() + area.height() - edgeSize;
		if (mouseY < topEdge) {
			return -PIXELS_PER_SECOND * Math.clamp((topEdge - mouseY) / edgeSize, 0, 1);
		}
		if (mouseY > bottomEdge) {
			return PIXELS_PER_SECOND * Math.clamp((mouseY - bottomEdge) / edgeSize, 0, 1);
		}
		return 0;
	}
}
