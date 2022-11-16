package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.util.IImmutableRect2i;

import java.util.Optional;

public interface IClickedIngredient<V> {

	ITypedIngredient<V> getTypedIngredient();

	Optional<IImmutableRect2i> getArea();

	boolean allowsCheating();

	/**
	 * Some GUIs (like vanilla) shouldn't allow JEI to click to set the focus,
	 * it would conflict with their normal behavior.
	 */
	boolean canOverrideVanillaClickHandler();
}
