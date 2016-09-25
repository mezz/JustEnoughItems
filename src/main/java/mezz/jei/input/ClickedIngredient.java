package mezz.jei.input;

public class ClickedIngredient<V> implements IClickedIngredient<V> {
	private final V value;
	private boolean allowsCheating;

	public ClickedIngredient(V value) {
		this.value = value;
	}

	@Override
	public V getValue() {
		return value;
	}

	public void setAllowsCheating() {
		this.allowsCheating = true;
	}

	@Override
	public boolean allowsCheating() {
		return allowsCheating;
	}
}
