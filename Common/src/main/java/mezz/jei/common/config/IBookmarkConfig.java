package mezz.jei.common.config;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.ingredients.RegisteredIngredients;

import java.util.List;

public interface IBookmarkConfig {
    void saveBookmarks(RegisteredIngredients registeredIngredients, List<ITypedIngredient<?>> list);

    void loadBookmarks(RegisteredIngredients registeredIngredients, BookmarkList bookmarkList);
}
