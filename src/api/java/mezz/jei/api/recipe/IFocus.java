package mezz.jei.api.recipe;

/**
 * The current search focus.
 * Set by the player when they look up the recipe. The ingredient being looked up is the focus.
 * This class is immutable, the value and mode do not change.
 *
 * Create a focus with {@link IRecipeManager#createFocus(Mode, Object)}.
 *
 * Use a null IFocus to signify no focus, like in the case of looking up categories of recipes.
 */
public interface IFocus<V> {
	/**
	 * The ingredient that is being focused on.
	 */
	V getValue();

	/**
	 * The focused recipe ingredient role.
	 * @since JEI 9.3.0
	 */
	default RecipeIngredientRole getRole() {
		// if not implemented, this calls the old getMode function for backward compatibility
		return getMode().toRole();
	}

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 * @deprecated since JEI 9.3.0. Use {@link RecipeIngredientRole} instead.
	 */
	@Deprecated
	enum Mode {
		INPUT, OUTPUT;

		/**
		 * Convert this legacy {@link IFocus} {@link Mode} into a {@link RecipeIngredientRole}.
		 *
		 * @since JEI 9.3.0
		 * @deprecated since JEI 9.3.0
		 */
		@Deprecated
		public RecipeIngredientRole toRole() {
			return switch (this) {
				case INPUT -> RecipeIngredientRole.INPUT;
				case OUTPUT -> RecipeIngredientRole.OUTPUT;
			};
		}
	}

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 * @deprecated since JEI 9.3.0. Use {@link #getRole()} instead.
	 */
	@Deprecated
	Mode getMode();
}
