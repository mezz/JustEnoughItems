package mezz.jei.common.recipes;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;

public interface IRecipeVisibility {
	<T> boolean isRecipeVisible(IRecipeCategory<T> recipeCategory, T recipe, IFocusGroup focuses);
}
