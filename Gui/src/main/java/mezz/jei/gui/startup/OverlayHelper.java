package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.overlay.IIngredientGridSource;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.config.IClientConfig;
import mezz.jei.gui.config.IIngredientGridConfig;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;

public final class OverlayHelper {
    private OverlayHelper() {}

    public static IngredientGridWithNavigation createIngredientGridWithNavigation(
        IIngredientGridSource ingredientFilter,
        IRegisteredIngredients registeredIngredients,
        IIngredientGridConfig ingredientGridConfig,
        IScreenHelper screenHelper,
        IModIdHelper modIdHelper,
        DrawableNineSliceTexture background,
        DrawableNineSliceTexture slotBackground,
        IInternalKeyMappings keyMappings,
        IEditModeConfig editModeConfig,
        IIngredientFilterConfig ingredientFilterConfig,
        IClientConfig clientConfig,
        IWorldConfig worldConfig,
        IConnectionToServer serverConnection,
        Textures textures,
        IColorHelper colorHelper
    ) {
        IngredientGrid ingredientListGrid = new IngredientGrid(
            registeredIngredients,
            ingredientGridConfig,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            worldConfig,
            screenHelper,
            modIdHelper,
            serverConnection,
            keyMappings,
            colorHelper
        );

        return new IngredientGridWithNavigation(
            ingredientFilter,
            screenHelper,
            ingredientListGrid,
            worldConfig,
            clientConfig,
            serverConnection,
            ingredientGridConfig,
            background,
            slotBackground,
            textures
        );
    }

    public static IngredientListOverlay createIngredientListOverlay(
        IRegisteredIngredients registeredIngredients,
        IScreenHelper screenHelper,
        IIngredientGridSource ingredientFilter,
        IFilterTextSource filterTextSource,
        IModIdHelper modIdHelper,
        IInternalKeyMappings keyMappings,
        IIngredientGridConfig ingredientGridConfig,
        IClientConfig clientConfig,
        IWorldConfig worldConfig,
        IEditModeConfig editModeConfig,
        IConnectionToServer serverConnection,
        IIngredientFilterConfig ingredientFilterConfig,
        Textures textures,
        IColorHelper colorHelper
    ) {
        IngredientGridWithNavigation ingredientListGridNavigation = createIngredientGridWithNavigation(
            ingredientFilter,
            registeredIngredients,
            ingredientGridConfig,
            screenHelper,
            modIdHelper,
            textures.getIngredientListBackground(),
            textures.getIngredientListSlotBackground(),
            keyMappings,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            worldConfig,
            serverConnection,
            textures,
            colorHelper
        );

        return new IngredientListOverlay(
            ingredientFilter,
            filterTextSource,
            registeredIngredients,
            screenHelper,
            ingredientListGridNavigation,
            clientConfig,
            worldConfig,
            serverConnection,
            textures,
            keyMappings
        );
    }

    public static BookmarkOverlay createBookmarkOverlay(
        IRegisteredIngredients registeredIngredients,
        IScreenHelper screenHelper,
        BookmarkList bookmarkList,
        IModIdHelper modIdHelper,
        IInternalKeyMappings keyMappings,
        IIngredientGridConfig bookmarkListConfig,
        IEditModeConfig editModeConfig,
        IIngredientFilterConfig ingredientFilterConfig,
        IClientConfig clientConfig,
        IWorldConfig worldConfig,
        IConnectionToServer serverConnection,
        Textures textures,
        IColorHelper colorHelper
    ) {
        IngredientGridWithNavigation bookmarkListGridNavigation = createIngredientGridWithNavigation(
            bookmarkList,
            registeredIngredients,
            bookmarkListConfig,
            screenHelper,
            modIdHelper,
            textures.getBookmarkListBackground(),
            textures.getBookmarkListSlotBackground(),
            keyMappings,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            worldConfig,
            serverConnection,
            textures,
            colorHelper
        );

        return new BookmarkOverlay(
            bookmarkList,
            textures,
            bookmarkListGridNavigation,
            clientConfig,
            worldConfig,
            screenHelper,
            serverConnection,
            keyMappings
        );
    }
}
