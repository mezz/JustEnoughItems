package mezz.jei.plugins.jei.ingredients;

public class DebugIngredient {
	private final int number;

	public DebugIngredient(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public DebugIngredient copy() {
		return new DebugIngredient(number);
	}
}
