package mezz.jei.gui.config;

import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IBookmark;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public interface IBookmarkConfig {
	void saveBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IIngredientManager ingredientManager, RegistryAccess registryAccess, List<IBookmark> bookmarks);

	void loadBookmarks(IRecipeManager recipeManager, IFocusFactory focusFactory, IIngredientManager ingredientManager, RegistryAccess registryAccess, BookmarkList bookmarkList);
}
