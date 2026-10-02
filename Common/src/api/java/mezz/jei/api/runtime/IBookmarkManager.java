package mezz.jei.api.runtime;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.ApiStatus;

/**
 * Gives access to JEI's ingredient and recipe bookmarks.
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

	/**
	 * Returns whether the recipe is bookmarked.
	 * Recipes are matched by recipe type and {@link IRecipeCategory#getRegistryName(Object)},
	 * rather than by recipe object identity.
	 *
	 * @return true if the recipe is bookmarked, or false if it is not bookmarked or recipe bookmarks are unsupported.
	 * @since 19.62.0
	 */
	default <R> boolean contains(RecipeType<R> recipeType, R recipe) {
		return false;
	}

	/**
	 * Adds the recipe to JEI's bookmarks.
	 *
	 * The recipe type must be registered with JEI. The recipe must have a non-null
	 * {@link IRecipeCategory#getRegistryName(Object)} and a displayable output or input ingredient.
	 * The bookmark's display ingredient is chosen from a recipe layout with no focuses.
	 *
	 * @return true if the recipe was added, or false if it was already bookmarked,
	 *         could not be bookmarked, or recipe bookmarks are unsupported.
	 * @since 19.62.0
	 */
	default <R> boolean add(RecipeType<R> recipeType, R recipe) {
		return false;
	}

	/**
	 * Removes the recipe from JEI's bookmarks.
	 * Recipes are matched by recipe type and {@link IRecipeCategory#getRegistryName(Object)},
	 * rather than by recipe object identity.
	 *
	 * @return true if the recipe was removed, or false if it was not bookmarked or recipe bookmarks are unsupported.
	 * @since 19.62.0
	 */
	default <R> boolean remove(RecipeType<R> recipeType, R recipe) {
		return false;
	}
}
