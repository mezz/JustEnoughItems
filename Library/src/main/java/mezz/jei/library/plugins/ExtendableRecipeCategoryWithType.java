package mezz.jei.library.plugins;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;

public interface ExtendableRecipeCategoryWithType<T, W extends IRecipeCategoryExtension> extends IExtendableRecipeCategory<T, W>, RecipeCategoryWithType<T> {
}
