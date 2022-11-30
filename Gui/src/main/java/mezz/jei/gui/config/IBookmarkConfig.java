package mezz.jei.gui.config;

import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.bookmarks.BookmarkList;

import java.util.List;

public interface IBookmarkConfig {
    void saveBookmarks(IRegisteredIngredients registeredIngredients, List<ITypedIngredient<?>> list);

    void loadBookmarks(IRegisteredIngredients registeredIngredients, BookmarkList bookmarkList);
}
