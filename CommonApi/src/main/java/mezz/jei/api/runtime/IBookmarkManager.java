package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.ApiStatus;

/**
 * Gives access to JEI's ingredient bookmarks.
 *
 * Get the instance from {@link IJeiRuntime#getBookmarkManager()}.
 *
 * @since 19.53.0
 */
@ApiStatus.NonExtendable
public interface IBookmarkManager {
	/**
	 * Returns whether the ingredient is bookmarked.
	 *
	 * @since 19.53.0
	 */
	boolean contains(ITypedIngredient<?> ingredient);

	/**
	 * Adds the ingredient to JEI's bookmarks.
	 *
	 * @return true if the ingredient was added, or false if it was already bookmarked.
	 * @since 19.53.0
	 */
	boolean add(ITypedIngredient<?> ingredient);

	/**
	 * Removes the ingredient from JEI's bookmarks.
	 *
	 * @return true if the ingredient was removed, or false if it was not bookmarked.
	 * @since 19.53.0
	 */
	boolean remove(ITypedIngredient<?> ingredient);
}
