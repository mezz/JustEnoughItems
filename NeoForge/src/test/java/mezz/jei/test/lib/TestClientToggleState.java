package mezz.jei.test.lib;

import mezz.jei.common.config.IClientToggleState;

import java.util.function.Consumer;

public class TestClientToggleState implements IClientToggleState {
	@Override
	public boolean isOverlayEnabled() {
		return true;
	}

	@Override
	public void toggleOverlayEnabled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOverlayEnabled(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runnable addOverlayEnabledListener(Consumer<Boolean> listener) {
		return () -> {};
	}

	@Override
	public boolean isEditModeEnabled() {
		return false;
	}

	@Override
	public void toggleEditModeEnabled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEditModeEnabled(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runnable addEditModeEnabledListener(Consumer<Boolean> listener) {
		return () -> {};
	}

	@Override
	public boolean isCheatItemsEnabled() {
		return false;
	}

	@Override
	public void toggleCheatItemsEnabled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCheatItemsEnabled(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runnable addCheatItemsEnabledListener(Consumer<Boolean> listener) {
		return () -> {};
	}

	@Override
	public boolean isBookmarkOverlayEnabled() {
		return true;
	}

	@Override
	public boolean isBookmarkEnabled() {
		return true;
	}

	@Override
	public void toggleBookmarkEnabled() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBookmarkEnabled(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Runnable addBookmarkEnabledListener(Consumer<Boolean> listener) {
		return () -> {};
	}

	@Override
	public void addEditModeToggleListener(IEditModeListener listener) {

	}

	@Override
	public void clearListeners() {

	}
}
