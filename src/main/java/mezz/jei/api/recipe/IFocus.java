package mezz.jei.api.recipe;

import javax.annotation.Nullable;

import mezz.jei.api.IRecipeRegistry;

/**
 * The current search focus.
 * Set by the player when they look up the recipe. The object being looked up is the focus.
 * This class is immutable, the value and mode do not change.
 * <p>
 * Create a focus with {@link IRecipeRegistry#createFocus(Mode, Object)}.
 */
public interface IFocus<V> {
	enum Mode {
		INPUT, OUTPUT, NONE
	}

	/**
	 * The object being focused on.
	 * When the mode is {@link Mode#NONE} there is no focused object, it is null.
	 */
	@Nullable
	V getValue();

	/**
	 * The focus mode.
	 * When a player looks up the recipes to make an item, that item is an {@link Mode#OUTPUT} focus.
	 * When a player looks up the uses for an item, that item is an {@link Mode#INPUT} focus.
	 * When the mode is {@link Mode#NONE} there is no focus, the recipe is being browsed as part of a category.
	 */
	Mode getMode();
}
