package mezz.jei.api.recipe.category.extensions;

import java.util.function.Function;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeWrapper;

public interface IExtendableRecipeCategory<T, W extends IRecipeWrapper> extends IRecipeCategory<T> {
	<R extends T> void addRecipeWrapperFactory(Class<? extends R> recipeClass, Function<R, ? extends W> recipeWrapperFactory);
}
