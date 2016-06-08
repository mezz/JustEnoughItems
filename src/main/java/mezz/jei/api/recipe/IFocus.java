package mezz.jei.api.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The current search focus. Set by the player when they look up the recipe. The object being looked up is the focus.
 */
public interface IFocus<V> {
	enum Mode {
		INPUT, OUTPUT, NONE
	}

	@Nullable
	V getValue();

	@Nonnull
	Mode getMode();
}
