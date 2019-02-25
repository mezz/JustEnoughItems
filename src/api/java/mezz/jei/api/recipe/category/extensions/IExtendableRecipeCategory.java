package mezz.jei.api.recipe.category.extensions;

import java.util.function.Function;

import mezz.jei.api.recipe.category.IRecipeCategory;

public interface IExtendableRecipeCategory<T, W extends IRecipeCategoryExtension> extends IRecipeCategory<T> {
	<R extends T> void addCategoryExtension(Class<? extends R> recipeClass, Function<R, ? extends W> extensionFactory);
}
