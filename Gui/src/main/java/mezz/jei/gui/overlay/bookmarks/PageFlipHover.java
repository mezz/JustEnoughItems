package mezz.jei.gui.overlay.bookmarks;

import org.jspecify.annotations.Nullable;

import java.util.function.LongSupplier;

/**
 * Tracks how long a bookmark drag has hovered over a page-flip edge of the bookmark list,
 * and requests a page flip after a delay. The delay restarts after each flip.
 */
final class PageFlipHover {
	static final long FLIP_DELAY_MS = 500;

	private final LongSupplier currentTimeMillis;
	private @Nullable Direction hoveredDirection;
	private long hoverStartMillis;

	enum Direction {
		NEXT,
		PREVIOUS
	}

	PageFlipHover(LongSupplier currentTimeMillis) {
		this.currentTimeMillis = currentTimeMillis;
	}

	@Nullable
	Direction update(@Nullable Direction hoveredDirection) {
		long now = this.currentTimeMillis.getAsLong();
		if (this.hoveredDirection != hoveredDirection) {
			this.hoveredDirection = hoveredDirection;
			this.hoverStartMillis = now;
			return null;
		}
		if (hoveredDirection == null || now - this.hoverStartMillis < FLIP_DELAY_MS) {
			return null;
		}
		this.hoverStartMillis = now;
		return hoveredDirection;
	}

}
