package mezz.jei.common.startup;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.overlay.IIngredientGridSource;
import mezz.jei.common.gui.overlay.IngredientGrid;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.RegisteredIngredients;

public final class OverlayHelper {
    private OverlayHelper() {}

    public static IngredientGridWithNavigation createIngredientGridWithNavigation(
        StartData data,
        IIngredientGridSource ingredientFilter,
        RegisteredIngredients registeredIngredients,
        IIngredientGridConfig ingredientGridConfig,
        GuiScreenHelper guiScreenHelper,
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
            guiScreenHelper,
            modIdHelper,
            data.serverConnection(),
            data.keyBindings()
        );

        return new IngredientGridWithNavigation(
            ingredientFilter,
            guiScreenHelper,
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
        RegisteredIngredients registeredIngredients,
        GuiScreenHelper guiScreenHelper,
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
            guiScreenHelper,
            modIdHelper,
            textures.getIngredientListBackground(),
            textures.getIngredientListSlotBackground()
        );

        return new IngredientListOverlay(
            ingredientFilter,
            filterTextSource,
            registeredIngredients,
            guiScreenHelper,
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
        RegisteredIngredients registeredIngredients,
        GuiScreenHelper guiScreenHelper,
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
            guiScreenHelper,
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
            guiScreenHelper,
            data.serverConnection(),
            data.keyBindings()
        );
    }
}
