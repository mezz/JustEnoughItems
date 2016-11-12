package mezz.jei.api.recipe;

import mezz.jei.api.IRecipeRegistry;

/**
 * The current search focus.
 * Set by the player when they look up the recipe. The object being looked up is the focus.
 * This class is immutable, the value and mode do not change.
 * <p>
 * Create a focus with {@link IRecipeRegistry#createFocus(Mode, Object)}.
 * <p>
 * Use a null IFocus to signify no focus, like in the case of looking up categories of recipes.
 */
public interface IFocus<V> {
	enum Mode {
		INPUT, OUTPUT
	}

	/**
	 * The object being focused on.
	 */
	V getValue();

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 */
	Mode getMode();
}
