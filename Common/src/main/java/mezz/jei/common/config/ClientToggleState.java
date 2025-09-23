package mezz.jei.common.config;

import java.util.ArrayList;
import java.util.List;

public class ClientToggleState implements IClientToggleState {
	private final List<IEditModeListener> editModeListeners = new ArrayList<>();

	private boolean overlayEnabled = true;
	private boolean cheatItemsEnabled = false;
	private boolean editModeEnabled = false;
	private boolean bookmarkOverlayEnabled = true;

	@Override
	public boolean isOverlayEnabled() {
		return overlayEnabled;
	}

	@Override
	public void toggleOverlayEnabled() {
		this.overlayEnabled = !this.overlayEnabled;
	}

	@Override
	public boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && bookmarkOverlayEnabled;
	}

	@Override
	public void toggleBookmarkEnabled() {
		setBookmarkEnabled(!bookmarkOverlayEnabled);
	}

	@Override
	public void setBookmarkEnabled(boolean value) {
		if (this.bookmarkOverlayEnabled != value) {
			this.bookmarkOverlayEnabled = value;
		}
	}

	@Override
	public boolean isCheatItemsEnabled() {
		return cheatItemsEnabled;
	}

	@Override
	public void toggleCheatItemsEnabled() {
		setCheatItemsEnabled(!cheatItemsEnabled);
	}

	@Override
	public void setCheatItemsEnabled(boolean value) {
		cheatItemsEnabled = value;
	}

	@Override
	public boolean isEditModeEnabled() {
		return editModeEnabled;
	}

	@Override
	public void toggleEditModeEnabled() {
		this.editModeEnabled = !this.editModeEnabled;
		editModeListeners.forEach(IEditModeListener::onEditModeChanged);
	}

	@Override
	public void addEditModeToggleListener(IEditModeListener listener) {
		editModeListeners.add(listener);
	}

	@Override
	public void clearListeners() {
		editModeListeners.clear();
	}
}
