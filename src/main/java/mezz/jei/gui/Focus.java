package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.recipe.IFocus;

public class Focus<V> implements IFocus<V> {
	@Nonnull
	private Mode mode;
	@Nullable
	private V value;
	private boolean allowsCheating;

	public Focus(@Nullable V value) {
		this.mode = Mode.NONE;
		this.value = value;
	}

	public Focus(@Nonnull Mode mode, @Nullable V value) {
		this.mode = mode;
		this.value = value;
	}

	@Nullable
	@Override
	public V getValue() {
		return value;
	}

	@Nonnull
	@Override
	public Mode getMode() {
		return mode;
	}

	public void setAllowsCheating() {
		this.allowsCheating = true;
	}

	public boolean allowsCheating() {
		return allowsCheating;
	}
}
