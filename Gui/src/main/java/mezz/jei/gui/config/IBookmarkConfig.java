package mezz.jei.gui.config;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.BookmarkList;

import java.util.List;

public interface IBookmarkConfig {
    void saveBookmarks(IIngredientManager ingredientManager, IBookmarkManager bookmarkManager, List<ITypedIngredient<?>> list);

    void loadBookmarks(IIngredientManager ingredientManager, IBookmarkManager bookmarkManager, BookmarkList bookmarkList);
}
