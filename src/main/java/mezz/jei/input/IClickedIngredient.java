package mezz.jei.input;

public interface IClickedIngredient<V> {

	V getValue();

	boolean allowsCheating();
}
