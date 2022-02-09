package mezz.jei.api.recipe.category.extensions;

import java.util.function.Function;
import java.util.function.Predicate;

import mezz.jei.api.recipe.category.IRecipeCategory;

public interface IExtendableRecipeCategory<T, W extends IRecipeCategoryExtension> extends IRecipeCategory<T> {
	/**
	 * @param recipeClass      the class of recipes to handle
	 * @param extensionFactory a factory that can turn recipes into recipe extensions
	 */
	<R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends W> extensionFactory);

	/**
	 * @param recipeClass      the class of recipes to handle
	 * @param extensionFilter  a filter that returns true for instances of the recipe that can be handled by the extensionFactory
	 * @param extensionFactory a factory that can turn recipes into recipe extensions
	 * @since 7.2.0
	 */
	<R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Predicate<R> extensionFilter, Function<R, ? extends W> extensionFactory);
}
