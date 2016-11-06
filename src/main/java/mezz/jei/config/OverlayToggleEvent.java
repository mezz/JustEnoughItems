package mezz.jei.config;

import net.minecraftforge.fml.common.eventhandler.Event;

public class OverlayToggleEvent extends Event {
	private final boolean overlayEnabled;

	public OverlayToggleEvent(boolean overlayEnabled) {
		this.overlayEnabled = overlayEnabled;
	}

	public boolean isOverlayEnabled() {
		return overlayEnabled;
	}
}
