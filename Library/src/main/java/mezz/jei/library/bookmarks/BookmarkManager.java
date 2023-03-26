package mezz.jei.library.bookmarks;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;

public class BookmarkManager implements IBookmarkManager {

    private static final Logger LOGGER = LogManager.getLogger();

    @Unmodifiable
    private final Map<IIngredientType<?>, BookmarkInfo<?>> bookmarkInfos;

    public BookmarkManager(Map<IIngredientType<?>, BookmarkInfo<?>> bookmarkInfos) {
        this.bookmarkInfos = Collections.unmodifiableMap(bookmarkInfos);
    }

    @Override
    public <V> ResourceLocation getIdentifier(V bookmark) {
        ErrorUtil.checkNotNull(bookmark, "bookmark");
        return getBookmarkInfo(bookmark.getClass()).getIdentifier();
    }

    @Override
    public <V> ResourceLocation getIdentifier(IIngredientType<V> ingredientType) {
        ErrorUtil.checkNotNull(ingredientType, "ingredientType");
        BookmarkInfo<?> bookmarkInfo = bookmarkInfos.get(ingredientType);
        if (bookmarkInfo == null) {
            throw new IllegalArgumentException("Unknown bookmark type: " + ingredientType);
        }
        return bookmarkInfo.getIdentifier();
    }

    @Override
    public <V> IIngredientHelper<V> getIngredientHelper(V bookmark) {
        ErrorUtil.checkNotNull(bookmark, "bookmark");
        return (IIngredientHelper<V>) getBookmarkInfo(bookmark.getClass()).getIngredientHelper();
    }

    @Override
    public <V> IIngredientHelper<V> getIngredientHelper(ResourceLocation identifier) {
        return (IIngredientHelper<V>) bookmarkInfos.values()
                .stream()
                .filter(info -> info.getIdentifier().equals(identifier))
                .findFirst()
                .map(BookmarkInfo::getIngredientHelper)
                .orElseThrow(() -> new IllegalArgumentException("Unknown bookmark identifier: " + identifier));
    }

    @Override
    public <V> IIngredientHelper<V> getIngredientHelper(IIngredientType<V> ingredientType) {
        ErrorUtil.checkNotNull(ingredientType, "ingredientType");
        BookmarkInfo<?> bookmarkInfo = bookmarkInfos.get(ingredientType);
        if (bookmarkInfo == null) {
            throw new IllegalArgumentException("Unknown bookmark type: " + ingredientType);
        }
        return (IIngredientHelper<V>) bookmarkInfo.getIngredientHelper();
    }

    @Override
    public <V> IIngredientRenderer<V> getIngredientRenderer(V bookmark) {
        ErrorUtil.checkNotNull(bookmark, "bookmark");
        return (IIngredientRenderer<V>) getBookmarkInfo(bookmark.getClass()).getRenderer();
    }

    @Override
    public <V> IIngredientRenderer<V> getIngredientRenderer(ResourceLocation identifier) {
        return (IIngredientRenderer<V>) bookmarkInfos.values()
                .stream()
                .filter(info -> info.getIdentifier().equals(identifier))
                .map(BookmarkInfo::getIngredientHelper)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown bookmark identifier: " + identifier));
    }

    @Override
    public <V> IIngredientRenderer<V> getIngredientRenderer(IIngredientType<V> ingredientType) {
        ErrorUtil.checkNotNull(ingredientType, "ingredientType");
        BookmarkInfo<?> bookmarkInfo = bookmarkInfos.get(ingredientType);
        if (bookmarkInfo == null) {
            throw new IllegalArgumentException("Unknown bookmark type: " + ingredientType);
        }
        return (IIngredientRenderer<V>) bookmarkInfo.getRenderer();
    }

    private <T> BookmarkInfo<T> getBookmarkInfo(Class<T> bookmarkType) {
        BookmarkInfo<T> bookmarkInfo = (BookmarkInfo<T>) bookmarkInfos.values().stream()
                .filter(info -> info.getIngredientType().getIngredientClass().equals(bookmarkType))
                .findFirst()
                .orElse(null);
        if (bookmarkInfo == null) {
            throw new IllegalArgumentException("Unknown bookmark type: " + bookmarkType);
        }
        return bookmarkInfo;
    }

}

