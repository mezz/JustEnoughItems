package mezz.jei.common.config;

import java.util.function.Consumer;

public interface IClientToggleState {
	boolean isOverlayEnabled();

	void toggleOverlayEnabled();

	void setOverlayEnabled(boolean value);

	Runnable addOverlayEnabledListener(Consumer<Boolean> listener);

	boolean isEditModeEnabled();

	void toggleEditModeEnabled();

	void setEditModeEnabled(boolean value);

	Runnable addEditModeEnabledListener(Consumer<Boolean> listener);

	boolean isCheatItemsEnabled();

	void toggleCheatItemsEnabled();

	void setCheatItemsEnabled(boolean value);

	Runnable addCheatItemsEnabledListener(Consumer<Boolean> listener);

	boolean isBookmarkOverlayEnabled();

	boolean isBookmarkEnabled();

	void toggleBookmarkEnabled();

	void setBookmarkEnabled(boolean value);

	Runnable addBookmarkEnabledListener(Consumer<Boolean> listener);

	void addEditModeToggleListener(IEditModeListener listener);

	void clearListeners();

	interface IEditModeListener {
		void onEditModeChanged();
	}
}
