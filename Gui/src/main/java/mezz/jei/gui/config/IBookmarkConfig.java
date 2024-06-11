package mezz.jei.gui.config;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.BookmarkList;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public interface IBookmarkConfig {
	void saveBookmarks(IIngredientManager ingredientManager, RegistryAccess registryAccess, List<ITypedIngredient<?>> list);

	void loadBookmarks(IIngredientManager ingredientManager, RegistryAccess registryAccess, BookmarkList bookmarkList);
}
