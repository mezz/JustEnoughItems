package mezz.jei.library.load.registration;

import com.google.common.base.Preconditions;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.ICustomBookmarkRegistration;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.bookmarks.BookmarkInfo;
import mezz.jei.library.bookmarks.BookmarkManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class BookmarkManagerBuilder implements ICustomBookmarkRegistration {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<IIngredientType<?>, BookmarkInfo<?>> registeredBookmarks = new java.util.HashMap<>();

    private final IngredientManagerBuilder ingredientManagerBuilder;

    public BookmarkManagerBuilder(IngredientManagerBuilder ingredientManagerBuilder) {
        this.ingredientManagerBuilder = ingredientManagerBuilder;
    }

    @Override
    public <B> void registerCustomBookmark(
            ResourceLocation identifier,
            IIngredientType<B> ingredientType,
            IIngredientHelper<B> ingredientHelper,
            IIngredientRenderer<B> ingredientRenderer
    ) {
        ErrorUtil.checkNotNull(identifier, "identifier");
        ErrorUtil.checkNotNull(ingredientType, "ingredientType");
        ErrorUtil.checkNotNull(ingredientHelper, "ingredientHelper");
        ErrorUtil.checkNotNull(ingredientRenderer, "ingredientRenderer");
        Preconditions.checkArgument(ingredientRenderer.getWidth() == 16,
                "the default ingredient renderer registered here will be used for drawing " +
                        "bookmarks in the bookmark list, and it must have a width of 16"
        );
        Preconditions.checkArgument(ingredientRenderer.getHeight() == 16,
                "the default ingredient renderer registered here will be used for drawing " +
                        "bookmarks in the bookmark  list, and it must have a height of 16"
        );

        if (registeredBookmarks.containsKey(ingredientType)) {
            throw new IllegalArgumentException("Bookmark type has already been registered: " + ingredientType.getIngredientClass());
        }

        BookmarkInfo<B> bookmarkInfo = new BookmarkInfo<>(identifier, ingredientType, ingredientHelper, ingredientRenderer);
        registeredBookmarks.put(ingredientType, bookmarkInfo);
        ingredientManagerBuilder.register(ingredientType, Collections.emptyList(), ingredientHelper, ingredientRenderer);
    }

    @Override
    public <B> void registerCustomBookmark(ResourceLocation identifier, IIngredientType<B> ingredientType) {
        ErrorUtil.checkNotNull(identifier, "identifier");
        ErrorUtil.checkNotNull(ingredientType, "ingredientType");

        if (registeredBookmarks.containsKey(ingredientType)) {
            throw new IllegalArgumentException("Bookmark type has already been registered: " + ingredientType.getIngredientClass());
        }

        Pair<IIngredientHelper<B>, IIngredientRenderer<B>> pair = ingredientManagerBuilder.getHelpers(ingredientType);
        BookmarkInfo<B> bookmarkInfo = new BookmarkInfo<>(identifier, ingredientType, pair.getLeft(), pair.getRight());
        registeredBookmarks.put(ingredientType, bookmarkInfo);
    }

    public IBookmarkManager build() {
        return new BookmarkManager(registeredBookmarks);
    }

}
