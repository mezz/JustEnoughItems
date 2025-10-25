package mezz.jei.gui.config;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.IBookmark;

import java.util.List;

public interface ILookupHistoryConfig {
	void save(IRecipeManager recipeManager, IIngredientManager ingredientManager, IFocusFactory focusFactory, List<IBookmark> bookmarks);

	List<IBookmark> load(IRecipeManager recipeManager, IIngredientManager ingredientManager,IFocusFactory focusFactory);
}
