package mezz.jei.input;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.util.ImmutableRect2i;
import org.jetbrains.annotations.Nullable;

public interface IClickedIngredient<V> {

	ITypedIngredient<V> getTypedIngredient();

	@Nullable
	ImmutableRect2i getArea();

	boolean allowsCheating();

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 */
	boolean canOverrideVanillaClickHandler();
}
