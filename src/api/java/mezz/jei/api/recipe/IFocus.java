package mezz.jei.api.recipe;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

/**
 * The current search focus.
 * Set by the player when they look up the recipe. The ingredient being looked up is the focus.
 * This class is immutable, the value and mode do not change.
 *
 * Create a focus with {@link IRecipeManager#createFocus(RecipeIngredientRole, IIngredientType, Object)}.
 *
 * Use a null IFocus to signify no focus, like in the case of looking up categories of recipes.
 */
public interface IFocus<V> {
	/**
	 * The ingredient that is being focused on.
	 * @since 9.3.0
	 */
	ITypedIngredient<V> getTypedValue();

	/**
	 * The focused recipe ingredient role.
	 * @since 9.3.0
	 */
	RecipeIngredientRole getRole();

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 * @deprecated Use {@link RecipeIngredientRole} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	enum Mode {
		INPUT, OUTPUT;

		/**
		 * Convert this legacy {@link IFocus} {@link Mode} into a {@link RecipeIngredientRole}.
		 *
		 * @since 9.3.0
		 */
		public RecipeIngredientRole toRole() {
			return switch (this) {
				case INPUT -> RecipeIngredientRole.INPUT;
				case OUTPUT -> RecipeIngredientRole.OUTPUT;
			};
		}
	}

	/**
	 * The ingredient that is being focused on.
	 *
	 * @deprecated use {@link #getTypedValue()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	default V getValue() {
		return getTypedValue().getIngredient();
	}

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 * @deprecated Use {@link #getRole()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	Mode getMode();
}
