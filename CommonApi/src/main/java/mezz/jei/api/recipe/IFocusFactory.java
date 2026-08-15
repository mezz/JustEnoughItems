package mezz.jei.api.recipe;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * {@link IFocusFactory} helps with creating {@link IFocus} for JEI.
 * Get an instance from {@link IJeiHelpers#getFocusFactory()}.
 */
@ApiStatus.NonExtendable
public interface IFocusFactory {
	/**
	 * Returns a new focus.
	 *
	 * @since 9.3.0
	 */
	<V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient);

	/**
	 * Returns a new focus.
	 *
	 * @since 10.3.0
	 */
	default <V> IFocus<V> createFocus(RecipeIngredientRole role, ITypedIngredient<V> typedIngredient) {
		return createFocus(role, typedIngredient.getType(), typedIngredient.getIngredient());
	}

	/**
	 * Returns a new focus group.
	 *
	 * @since 10.3.0
	 */
	IFocusGroup createFocusGroup(Collection<? extends IFocus<?>> focuses);

	/**
	 * Returns an empty focus group.
	 *
	 * @since 10.3.0
	 */
	IFocusGroup getEmptyFocusGroup();

	/**
	 * Returns a new focus.
	 *
	 * @deprecated Use {@link #createFocus(RecipeIngredientRole, IIngredientType, Object)} instead.
	 */
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);
}
