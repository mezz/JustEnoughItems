package mezz.jei.gui;

import mezz.jei.api.recipe.IFocus;

public class Focus<V> implements IFocus<V> {
	private final Mode mode;
	private final V value;

	public Focus(Mode mode, V value) {
		this.mode = mode;
		this.value = value;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public Mode getMode() {
		return mode;
	}
}
