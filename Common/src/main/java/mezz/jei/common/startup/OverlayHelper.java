package mezz.jei.common.startup;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.overlay.IIngredientGridSource;
import mezz.jei.common.gui.overlay.IngredientGrid;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.gui.textures.Textures;

public final class OverlayHelper {
    private OverlayHelper() {}

    public static IngredientGridWithNavigation createIngredientGridWithNavigation(
        StartData data,
        IIngredientGridSource ingredientFilter,
        IRegisteredIngredients registeredIngredients,
        IIngredientGridConfig ingredientGridConfig,
        IScreenHelper screenHelper,
        IModIdHelper modIdHelper,
        DrawableNineSliceTexture background,
        DrawableNineSliceTexture slotBackground
    ) {
        ConfigData configData = data.configData();
        IngredientGrid ingredientListGrid = new IngredientGrid(
            registeredIngredients,
            ingredientGridConfig,
            configData.editModeConfig(),
            configData.ingredientFilterConfig(),
            configData.clientConfig(),
            configData.worldConfig(),
            screenHelper,
            modIdHelper,
            data.serverConnection(),
            data.keyBindings()
        );

        return new IngredientGridWithNavigation(
            ingredientFilter,
            screenHelper,
            ingredientListGrid,
            configData.worldConfig(),
            configData.clientConfig(),
            data.serverConnection(),
            ingredientGridConfig,
            background,
            slotBackground,
            data.textures()
        );
    }

    public static IngredientListOverlay createIngredientListOverlay(
        StartData data,
        IRegisteredIngredients registeredIngredients,
        IScreenHelper screenHelper,
        IIngredientGridSource ingredientFilter,
        IFilterTextSource filterTextSource,
        IModIdHelper modIdHelper
    ) {
        ConfigData configData = data.configData();

        Textures textures = data.textures();
        IngredientGridWithNavigation ingredientListGridNavigation = createIngredientGridWithNavigation(
            data,
            ingredientFilter,
            registeredIngredients,
            configData.ingredientListConfig(),
            screenHelper,
            modIdHelper,
            textures.getIngredientListBackground(),
            textures.getIngredientListSlotBackground()
        );

        return new IngredientListOverlay(
            ingredientFilter,
            filterTextSource,
            registeredIngredients,
            screenHelper,
            ingredientListGridNavigation,
            configData.clientConfig(),
            configData.worldConfig(),
            data.serverConnection(),
            textures,
            data.keyBindings()
        );
    }

    public static BookmarkOverlay createBookmarkOverlay(
        StartData data,
        IRegisteredIngredients registeredIngredients,
        IScreenHelper screenHelper,
        BookmarkList bookmarkList,
        IModIdHelper modIdHelper
    ) {
        ConfigData configData = data.configData();

        Textures textures = data.textures();

        IngredientGridWithNavigation bookmarkListGridNavigation = createIngredientGridWithNavigation(
            data,
            bookmarkList,
            registeredIngredients,
            configData.bookmarkListConfig(),
            screenHelper,
            modIdHelper,
            textures.getBookmarkListBackground(),
            textures.getBookmarkListSlotBackground()
        );

        return new BookmarkOverlay(
            bookmarkList,
            textures,
            bookmarkListGridNavigation,
            configData.clientConfig(),
            configData.worldConfig(),
            screenHelper,
            data.serverConnection(),
            data.keyBindings()
        );
    }
}
