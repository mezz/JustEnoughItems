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
	private @Nullable Button hoveredButton;
	private long hoverStartMillis;

	enum Button {
		NEXT,
		BACK
	}

	PageFlipHover(LongSupplier currentTimeMillis) {
		this.currentTimeMillis = currentTimeMillis;
	}

	@Nullable
	Button update(@Nullable Button hoveredButton) {
		long now = this.currentTimeMillis.getAsLong();
		if (this.hoveredButton != hoveredButton) {
			this.hoveredButton = hoveredButton;
			this.hoverStartMillis = now;
			return null;
		}
		if (hoveredButton == null || now - this.hoverStartMillis < FLIP_DELAY_MS) {
			return null;
		}
		this.hoverStartMillis = now;
		return hoveredButton;
	}

	void reset() {
		this.hoveredButton = null;
	}
}
