package mezz.jei.config;

import net.minecraftforge.fml.common.eventhandler.Event;

public class BookmarkOverlayToggleEvent extends Event {
	private final boolean bookmarkOverlayEnabled;

	public BookmarkOverlayToggleEvent(boolean bookmarkOverlayEnabled) {
		this.bookmarkOverlayEnabled = bookmarkOverlayEnabled;
	}

	public boolean isBookmarkOverlayEnabled() {
		return bookmarkOverlayEnabled;
	}
}
