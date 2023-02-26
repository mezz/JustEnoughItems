package mezz.jei.common.config;

public interface IClientToggleState {
	boolean isOverlayEnabled();

	void toggleOverlayEnabled();

	boolean isEditModeEnabled();

	void toggleEditModeEnabled();

	boolean isCheatItemsEnabled();

	void toggleCheatItemsEnabled();

	void setCheatItemsEnabled(boolean value);

	boolean isBookmarkOverlayEnabled();

	void toggleBookmarkEnabled();

	void setBookmarkEnabled(boolean value);
}
