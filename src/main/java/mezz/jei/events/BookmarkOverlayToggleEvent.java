package mezz.jei.events;

public class BookmarkOverlayToggleEvent extends JeiEvent {
	private final boolean bookmarkOverlayEnabled;

	@SuppressWarnings("unused") // needed for event bus
	public BookmarkOverlayToggleEvent() {
		this.bookmarkOverlayEnabled = false;
	}

	public BookmarkOverlayToggleEvent(boolean bookmarkOverlayEnabled) {
		this.bookmarkOverlayEnabled = bookmarkOverlayEnabled;
	}

	public boolean isBookmarkOverlayEnabled() {
		return bookmarkOverlayEnabled;
	}
}
