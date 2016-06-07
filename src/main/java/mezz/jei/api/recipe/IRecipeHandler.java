package mezz.jei.api.recipe;

import javax.annotation.Nonnull;

/**
 * An IRecipeHandler provides information about one Recipe Class.
 */
public interface IRecipeHandler<T> {

	/** Returns the class of the Recipe handled by this IRecipeHandler. */
	@Nonnull
	Class<T> getRecipeClass();

	/**
	 * Returns this recipe's unique category id.
	 *
	 * @deprecated since 3.5.0. Use {@link #getRecipeCategoryUid(Object)}
	 */
	@Deprecated
	@Nonnull
	String getRecipeCategoryUid();

	/**
	 * Returns this recipe's unique category id.
	 *
	 * @since 3.5.0
	 */
	@Nonnull
	String getRecipeCategoryUid(@Nonnull T recipe);

	/** Returns a recipe wrapper for the given recipe. */
	@Nonnull
	IRecipeWrapper getRecipeWrapper(@Nonnull T recipe);

	/** Returns true if a recipe is valid and can be used. */
	boolean isRecipeValid(@Nonnull T recipe);
}
