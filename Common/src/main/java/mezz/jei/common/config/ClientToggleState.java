package mezz.jei.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClientToggleState implements IClientToggleState {
	private final List<IEditModeListener> editModeListeners = new ArrayList<>();
	private final List<Consumer<Boolean>> overlayEnabledListeners = new ArrayList<>();
	private final List<Consumer<Boolean>> bookmarkEnabledListeners = new ArrayList<>();
	private final List<Consumer<Boolean>> cheatItemsEnabledListeners = new ArrayList<>();
	private final List<Consumer<Boolean>> editModeEnabledListeners = new ArrayList<>();

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
		setOverlayEnabled(!overlayEnabled);
	}

	@Override
	public void setOverlayEnabled(boolean value) {
		if (this.overlayEnabled != value) {
			this.overlayEnabled = value;
			notifyListeners(overlayEnabledListeners, value);
		}
	}

	@Override
	public Runnable addOverlayEnabledListener(Consumer<Boolean> listener) {
		return addListener(overlayEnabledListeners, listener);
	}

	@Override
	public boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && isBookmarkEnabled();
	}

	@Override
	public boolean isBookmarkEnabled() {
		return bookmarkOverlayEnabled;
	}

	@Override
	public void toggleBookmarkEnabled() {
		setBookmarkEnabled(!bookmarkOverlayEnabled);
	}

	@Override
	public void setBookmarkEnabled(boolean value) {
		if (this.bookmarkOverlayEnabled != value) {
			this.bookmarkOverlayEnabled = value;
			notifyListeners(bookmarkEnabledListeners, value);
		}
	}

	@Override
	public Runnable addBookmarkEnabledListener(Consumer<Boolean> listener) {
		return addListener(bookmarkEnabledListeners, listener);
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
		if (this.cheatItemsEnabled != value) {
			this.cheatItemsEnabled = value;
			notifyListeners(cheatItemsEnabledListeners, value);
		}
	}

	@Override
	public Runnable addCheatItemsEnabledListener(Consumer<Boolean> listener) {
		return addListener(cheatItemsEnabledListeners, listener);
	}

	@Override
	public boolean isEditModeEnabled() {
		return editModeEnabled;
	}

	@Override
	public void toggleEditModeEnabled() {
		setEditModeEnabled(!editModeEnabled);
	}

	@Override
	public void setEditModeEnabled(boolean value) {
		if (this.editModeEnabled != value) {
			this.editModeEnabled = value;
			editModeListeners.forEach(IEditModeListener::onEditModeChanged);
			notifyListeners(editModeEnabledListeners, value);
		}
	}

	@Override
	public Runnable addEditModeEnabledListener(Consumer<Boolean> listener) {
		return addListener(editModeEnabledListeners, listener);
	}

	@Override
	public void addEditModeToggleListener(IEditModeListener listener) {
		editModeListeners.add(listener);
	}

	@Override
	public void clearListeners() {
		editModeListeners.clear();
		overlayEnabledListeners.clear();
		bookmarkEnabledListeners.clear();
		cheatItemsEnabledListeners.clear();
		editModeEnabledListeners.clear();
	}

	private static <T> Runnable addListener(List<Consumer<T>> listeners, Consumer<T> listener) {
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	private static <T> void notifyListeners(List<Consumer<T>> listeners, T value) {
		List.copyOf(listeners).forEach(listener -> listener.accept(value));
	}
}
