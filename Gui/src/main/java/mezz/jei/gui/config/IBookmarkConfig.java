package mezz.jei.gui.config;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.bookmarks.BookmarkList;

import java.util.List;

public interface IBookmarkConfig {
    void saveBookmarks(IIngredientManager ingredientManager, List<ITypedIngredient<?>> list);

    void loadBookmarks(IIngredientManager ingredientManager, BookmarkList bookmarkList);
}
