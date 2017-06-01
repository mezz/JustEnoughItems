package mezz.jei.test.lib;

public class TestIngredient {
	private final int number;

	public TestIngredient(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public TestIngredient copy() {
		return new TestIngredient(number);
	}

	@Override
	public String toString() {
		return "TestIngredient#" + number;
	}
}
