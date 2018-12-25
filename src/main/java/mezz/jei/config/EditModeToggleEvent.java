package mezz.jei.config;

import net.minecraftforge.eventbus.api.Event;

public class EditModeToggleEvent extends Event {
	private final boolean editModeEnabled;

	public EditModeToggleEvent(boolean editModeEnabled) {
		this.editModeEnabled = editModeEnabled;
	}

	public boolean isEditModeEnabled() {
		return editModeEnabled;
	}
}
