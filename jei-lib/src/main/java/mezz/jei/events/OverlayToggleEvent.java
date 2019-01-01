package mezz.jei.events;

public class OverlayToggleEvent extends JeiEvent {
	private final boolean overlayEnabled;

	@SuppressWarnings("unused") // needed for event bus
	public OverlayToggleEvent() {
		this.overlayEnabled = false;
	}

	public OverlayToggleEvent(boolean overlayEnabled) {
		this.overlayEnabled = overlayEnabled;
	}

	public boolean isOverlayEnabled() {
		return overlayEnabled;
	}
}
