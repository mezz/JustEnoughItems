package mezz.jei.config;

public interface IWorldConfig {
	boolean isOverlayEnabled();

	void toggleOverlayEnabled();

	boolean isEditModeEnabled();

	void toggleEditModeEnabled();

	boolean isCheatItemsEnabled();

	boolean isDeleteItemsInCheatModeActive();

	void toggleCheatItemsEnabled();

	void setCheatItemsEnabled(boolean value);

	boolean isBookmarkOverlayEnabled();

	void toggleBookmarkEnabled();

	void setBookmarkEnabled(boolean value);
}
