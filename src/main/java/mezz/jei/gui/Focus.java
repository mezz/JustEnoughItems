package mezz.jei.gui;

import javax.annotation.Nullable;

import mezz.jei.api.recipe.IFocus;

public class Focus<V> implements IFocus<V> {
	private final Mode mode;
	@Nullable
	private final V value;

	public Focus(@Nullable V value) {
		this.mode = Mode.NONE;
		this.value = value;
	}

	public Focus(Mode mode, @Nullable V value) {
		this.mode = mode;
		this.value = value;
	}

	@Nullable
	@Override
	public V getValue() {
		return value;
	}

	@Override
	public Mode getMode() {
		return mode;
	}
}
