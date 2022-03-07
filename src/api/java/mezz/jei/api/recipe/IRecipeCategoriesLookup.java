package mezz.jei.api.recipe;

import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * This is a helper class for looking up recipe categories.
 * Create one with {@link IRecipeManager#createRecipeCategoryLookup()},
 * then set its properties and call {@link #get()} to get the results.
 *
 * @since 9.5.0
 */
public interface IRecipeCategoriesLookup {
	/**
	 * Limit the results to only recipe categories for the given types.
	 *
	 * @since 9.5.0
	 */
	IRecipeCategoriesLookup limitTypes(Collection<RecipeType<?>> recipeTypes);

	/**
	 * Limit the results to only recipe categories matching the given focuses.
	 *
	 * @since 9.5.0
	 */
	IRecipeCategoriesLookup limitFocus(Collection<? extends IFocus<?>> focuses);

	/**
	 * By default, hidden results are not returned.
	 * Calling this will make this lookup include hidden recipe categories.
	 *
	 * @since 9.5.0
	 */
	IRecipeCategoriesLookup includeHidden();

	/**
	 * Get the recipe category results for this lookup.
	 *
	 * @since 9.5.0
	 */
	Stream<IRecipeCategory<?>> get();
}
