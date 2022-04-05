package mezz.jei.common.startup;

import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.overlay.IFilterTextSource;
import mezz.jei.common.gui.overlay.IIngredientGridSource;
import mezz.jei.common.gui.overlay.IngredientGrid;
import mezz.jei.common.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.ingredients.RegisteredIngredients;

public final class OverlayHelper {
    private OverlayHelper() {}

    public static IngredientGridWithNavigation createIngredientGridWithNavigation(
        StartData data,
        IIngredientGridSource ingredientFilter,
        RegisteredIngredients registeredIngredients,
        IIngredientGridConfig ingredientGridConfig,
        GuiScreenHelper guiScreenHelper,
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
            data.modIdHelper(),
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
        IFilterTextSource filterTextSource
    ) {
        ConfigData configData = data.configData();

        IngredientGridWithNavigation ingredientListGridNavigation = createIngredientGridWithNavigation(
            data,
            ingredientFilter,
            registeredIngredients,
            configData.ingredientListConfig(),
            guiScreenHelper,
            data.textures().getIngredientListBackground(),
            data.textures().getIngredientListSlotBackground()
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
            data.textures(),
            data.keyBindings()
        );
    }

    public static BookmarkOverlay createBookmarkOverlay(
        StartData data,
        RegisteredIngredients registeredIngredients,
        GuiScreenHelper guiScreenHelper,
        IIngredientGridSource ingredientFilter,
        BookmarkList bookmarkList
    ) {
        ConfigData configData = data.configData();

        IngredientGridWithNavigation bookmarkListGridNavigation = createIngredientGridWithNavigation(
            data,
            ingredientFilter,
            registeredIngredients,
            configData.bookmarkListConfig(),
            guiScreenHelper,
            data.textures().getBookmarkListBackground(),
            data.textures().getBookmarkListSlotBackground()
        );

        return new BookmarkOverlay(
            bookmarkList,
            data.textures(),
            bookmarkListGridNavigation,
            configData.clientConfig(),
            configData.worldConfig(),
            guiScreenHelper,
            data.serverConnection(),
            data.keyBindings()
        );
    }
}
