package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.common.gui.ingredients.IListElement;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.input.ClientInputHandler;
import mezz.jei.common.input.ICharTypedHandler;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.handlers.DragRouter;
import mezz.jei.gui.input.handlers.GlobalInputHandler;
import mezz.jei.gui.input.handlers.GuiAreaInputHandler;
import mezz.jei.gui.input.handlers.UserInputRouter;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.gui.util.CheatUtil;
import mezz.jei.common.util.LoggedTimer;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.config.IClientConfig;
import mezz.jei.gui.config.IIngredientFilterConfig;
import mezz.jei.gui.config.IIngredientGridConfig;
import mezz.jei.gui.config.IJeiClientConfigs;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.gui.ingredients.IIngredientSorter;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientFilterApi;
import mezz.jei.gui.ingredients.IngredientSorter;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.GuiContainerWrapper;
import mezz.jei.gui.input.handlers.BookmarkInputHandler;
import mezz.jei.gui.input.handlers.EditInputHandler;
import mezz.jei.gui.input.handlers.FocusInputHandler;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class JeiGuiStarter {
    private static final Logger LOGGER = LogManager.getLogger();

    public static JeiEventHandlers start(IJeiRuntime jeiRuntime) {
        LOGGER.info("Starting JEI GUI");
        LoggedTimer timer = new LoggedTimer();

        IConnectionToServer serverConnection = Internal.getServerConnection();
        Textures textures = Internal.getTextures();
        IInternalKeyMappings keyMappings = Internal.getKeyMappings();

        IScreenHelper screenHelper = jeiRuntime.getScreenHelper();
        IRecipeTransferManager recipeTransferManager = jeiRuntime.getRecipeTransferManager();
        IRecipeManager recipeManager = jeiRuntime.getRecipeManager();
        IIngredientVisibility ingredientVisibility = jeiRuntime.getIngredientVisibility();
        IIngredientManager ingredientManager = jeiRuntime.getIngredientManager();
        IEditModeConfig editModeConfig = jeiRuntime.getEditModeConfig();

        IJeiHelpers jeiHelpers = jeiRuntime.getJeiHelpers();
        IColorHelper colorHelper = jeiHelpers.getColorHelper();
        IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
        IRegisteredIngredients registeredIngredients = jeiHelpers.getRegisteredIngredients();

        IFilterTextSource filterTextSource = new FilterTextSource();

        timer.start("Building ingredient list");
        NonNullList<IListElement<?>> ingredientList = IngredientListElementFactory.createBaseList(registeredIngredients);
        timer.stop();

        timer.start("Building ingredient filter");
        GuiConfigData configData = GuiConfigData.create();
        ModNameSortingConfig modNameSortingConfig = configData.modNameSortingConfig();
        IngredientTypeSortingConfig ingredientTypeSortingConfig = configData.ingredientTypeSortingConfig();
        IWorldConfig worldConfig = Internal.getWorldConfig();
        IBookmarkConfig bookmarkConfig = configData.bookmarkConfig();

        IJeiClientConfigs jeiClientConfigs = configData.jeiClientConfigs();
        IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
        IIngredientGridConfig ingredientListConfig = jeiClientConfigs.getIngredientListConfig();
        IIngredientGridConfig bookmarkListConfig = jeiClientConfigs.getBookmarkListConfig();
        IIngredientFilterConfig ingredientFilterConfig = jeiClientConfigs.getIngredientFilterConfig();

        IIngredientSorter ingredientSorter = new IngredientSorter(
            clientConfig,
            modNameSortingConfig,
            ingredientTypeSortingConfig
        );

        IngredientFilter ingredientFilter = new IngredientFilter(
            filterTextSource,
            clientConfig,
            ingredientFilterConfig,
            registeredIngredients,
            ingredientSorter,
            ingredientList,
            modIdHelper,
            ingredientVisibility,
            colorHelper
        );
        ingredientManager.addIngredientListener(ingredientFilter);
        timer.stop();

        IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, filterTextSource);
        jeiRuntime.setIngredientFilter(ingredientFilterApi);

        CheatUtil cheatUtil = new CheatUtil(registeredIngredients);
        IngredientListOverlay ingredientListOverlay = OverlayHelper.createIngredientListOverlay(
            registeredIngredients,
            screenHelper,
            ingredientFilter,
            filterTextSource,
            modIdHelper,
            keyMappings,
            ingredientListConfig,
            clientConfig,
            worldConfig,
            editModeConfig,
            serverConnection,
            ingredientFilterConfig,
            textures,
            colorHelper,
            cheatUtil
        );
        jeiRuntime.setIngredientListOverlay(ingredientListOverlay);

        BookmarkList bookmarkList = new BookmarkList(registeredIngredients, bookmarkConfig);
        bookmarkConfig.loadBookmarks(registeredIngredients, bookmarkList);

        BookmarkOverlay bookmarkOverlay = OverlayHelper.createBookmarkOverlay(
            registeredIngredients,
            screenHelper,
            bookmarkList,
            modIdHelper,
            keyMappings,
            bookmarkListConfig,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            worldConfig,
            serverConnection,
            textures,
            colorHelper,
            cheatUtil
        );
        jeiRuntime.setBookmarkOverlay(bookmarkOverlay);

        GuiEventHandler guiEventHandler = new GuiEventHandler(
            screenHelper,
            bookmarkOverlay,
            ingredientListOverlay
        );

        RecipesGui recipesGui = new RecipesGui(
            recipeManager,
            recipeTransferManager,
            registeredIngredients,
            modIdHelper,
            clientConfig,
            textures,
            ingredientVisibility,
            keyMappings
        );
        jeiRuntime.setRecipesGui(recipesGui);

        CombinedRecipeFocusSource recipeFocusSource = new CombinedRecipeFocusSource(
            recipesGui,
            ingredientListOverlay,
            bookmarkOverlay,
            new GuiContainerWrapper(screenHelper)
        );

        List<ICharTypedHandler> charTypedHandlers = List.of(
            ingredientListOverlay
        );

        UserInputRouter userInputRouter = new UserInputRouter(
            new EditInputHandler(recipeFocusSource, worldConfig, editModeConfig),
            ingredientListOverlay.createInputHandler(),
            bookmarkOverlay.createInputHandler(),
            new FocusInputHandler(recipeFocusSource, recipesGui),
            new BookmarkInputHandler(recipeFocusSource, bookmarkList),
            new GlobalInputHandler(worldConfig),
            new GuiAreaInputHandler(registeredIngredients, screenHelper, recipesGui)
        );

        DragRouter dragRouter = new DragRouter(
            ingredientListOverlay.createDragHandler()
        );
        ClientInputHandler clientInputHandler = new ClientInputHandler(
            charTypedHandlers,
            userInputRouter,
            dragRouter,
            keyMappings
        );

        return new JeiEventHandlers(
            guiEventHandler,
            clientInputHandler
        );
    }
}
