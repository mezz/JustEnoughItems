package mezz.jei.events;

public class EditModeToggleEvent extends JeiEvent {
	private final boolean editModeEnabled;

	@SuppressWarnings("unused") // needed for event bus
	public EditModeToggleEvent() {
		editModeEnabled = false;
	}

	public EditModeToggleEvent(boolean editModeEnabled) {
		this.editModeEnabled = editModeEnabled;
	}

	public boolean isEditModeEnabled() {
		return editModeEnabled;
	}
}
