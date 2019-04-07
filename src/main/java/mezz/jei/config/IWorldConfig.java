package mezz.jei.config;

public interface IWorldConfig extends IFilterTextSource {
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

	@Override
	String getFilterText();

	boolean setFilterText(String filterText);

	void saveFilterText();
}
