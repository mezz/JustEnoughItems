package mezz.jei.api.recipe;

import javax.annotation.Nonnull;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;

/**
 * An IRecipeHandler provides information about one Recipe Class.
 *
 * Its main purpose is to tie recipe classes to an {@link IRecipeCategory}
 * and convert recipes to {@link IRecipeWrapper} with {@link #getRecipeWrapper(Object)}.
 *
 * Plugins implement these to handle their recipes, and register them with {@link IModRegistry#addRecipeHandlers(IRecipeHandler[])}.
 * You can find the registered Recipe Handler for a recipe class with {@link IRecipeRegistry#getRecipeHandler(Class)}
 */
public interface IRecipeHandler<T> {

	/**
	 * Returns the class of the Recipe handled by this IRecipeHandler.
	 */
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
	 *
	 * @see IRecipeCategory#getUid()
	 * @see VanillaRecipeCategoryUid
	 */
	@Nonnull
	String getRecipeCategoryUid(@Nonnull T recipe);

	/**
	 * Returns a recipe wrapper for the given recipe.
	 */
	@Nonnull
	IRecipeWrapper getRecipeWrapper(@Nonnull T recipe);

	/**
	 * Returns true if a recipe is valid and can be used.
	 */
	boolean isRecipeValid(@Nonnull T recipe);
}
