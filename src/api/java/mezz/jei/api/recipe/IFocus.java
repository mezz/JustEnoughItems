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
	enum Mode {
		INPUT, OUTPUT
	}

	/**
	 * The ingredient that is being focused on.
	 */
	V getValue();

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 */
	Mode getMode();
}
