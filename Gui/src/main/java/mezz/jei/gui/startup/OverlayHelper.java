package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.overlay.IIngredientGridSource;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.util.CheatUtil;

public final class OverlayHelper {
    private OverlayHelper() {}

    public static IngredientGridWithNavigation createIngredientGridWithNavigation(
        IIngredientGridSource ingredientFilter,
        IIngredientManager ingredientManager,
        IIngredientGridConfig ingredientGridConfig,
        IModIdHelper modIdHelper,
        DrawableNineSliceTexture background,
        DrawableNineSliceTexture slotBackground,
        IInternalKeyMappings keyMappings,
        IEditModeConfig editModeConfig,
        IIngredientFilterConfig ingredientFilterConfig,
        IClientConfig clientConfig,
        IClientToggleState toggleState,
        IConnectionToServer serverConnection,
        Textures textures,
        IColorHelper colorHelper,
        CheatUtil cheatUtil
    ) {
        IngredientGrid ingredientListGrid = new IngredientGrid(
            ingredientManager,
            ingredientGridConfig,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            toggleState,
            modIdHelper,
            serverConnection,
            keyMappings,
            colorHelper,
            cheatUtil
        );

        return new IngredientGridWithNavigation(
            ingredientFilter,
            ingredientListGrid,
            toggleState,
            clientConfig,
            serverConnection,
            ingredientGridConfig,
            background,
            slotBackground,
            textures,
            cheatUtil
        );
    }

    public static IngredientListOverlay createIngredientListOverlay(
        IIngredientManager ingredientManager,
        IScreenHelper screenHelper,
        IIngredientGridSource ingredientFilter,
        IFilterTextSource filterTextSource,
        IModIdHelper modIdHelper,
        IInternalKeyMappings keyMappings,
        IIngredientGridConfig ingredientGridConfig,
        IClientConfig clientConfig,
        IClientToggleState toggleState,
        IEditModeConfig editModeConfig,
        IConnectionToServer serverConnection,
        IIngredientFilterConfig ingredientFilterConfig,
        Textures textures,
        IColorHelper colorHelper,
        CheatUtil cheatUtil
    ) {
        IngredientGridWithNavigation ingredientListGridNavigation = createIngredientGridWithNavigation(
            ingredientFilter,
            ingredientManager,
            ingredientGridConfig,
            modIdHelper,
            textures.getIngredientListBackground(),
            textures.getIngredientListSlotBackground(),
            keyMappings,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            toggleState,
            serverConnection,
            textures,
            colorHelper,
            cheatUtil
        );

        return new IngredientListOverlay(
            ingredientFilter,
            filterTextSource,
            ingredientManager,
            screenHelper,
            ingredientListGridNavigation,
            clientConfig,
            toggleState,
            serverConnection,
            textures,
            keyMappings,
            cheatUtil
        );
    }

    public static BookmarkOverlay createBookmarkOverlay(
        IIngredientManager ingredientManager,
        IScreenHelper screenHelper,
        BookmarkList bookmarkList,
        IModIdHelper modIdHelper,
        IInternalKeyMappings keyMappings,
        IIngredientGridConfig bookmarkListConfig,
        IEditModeConfig editModeConfig,
        IIngredientFilterConfig ingredientFilterConfig,
        IClientConfig clientConfig,
        IClientToggleState toggleState,
        IConnectionToServer serverConnection,
        Textures textures,
        IColorHelper colorHelper,
        CheatUtil cheatUtil
    ) {
        IngredientGridWithNavigation bookmarkListGridNavigation = createIngredientGridWithNavigation(
            bookmarkList,
            ingredientManager,
            bookmarkListConfig,
            modIdHelper,
            textures.getBookmarkListBackground(),
            textures.getBookmarkListSlotBackground(),
            keyMappings,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            toggleState,
            serverConnection,
            textures,
            colorHelper,
            cheatUtil
        );

        return new BookmarkOverlay(
            bookmarkList,
            textures,
            bookmarkListGridNavigation,
            clientConfig,
            toggleState,
            screenHelper,
            serverConnection,
            keyMappings,
            cheatUtil
        );
    }
}
