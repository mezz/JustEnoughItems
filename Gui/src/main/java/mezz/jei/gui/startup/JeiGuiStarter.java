package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.util.LoggedTimer;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.ingredients.IIngredientSorter;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientFilterApi;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.ingredients.IngredientSorter;
import mezz.jei.gui.input.ClientInputHandler;
import mezz.jei.gui.input.CombinedRecipeFocusSource;
import mezz.jei.gui.input.GuiContainerWrapper;
import mezz.jei.gui.input.ICharTypedHandler;
import mezz.jei.gui.input.handlers.BookmarkInputHandler;
import mezz.jei.gui.input.handlers.DragRouter;
import mezz.jei.gui.input.handlers.EditInputHandler;
import mezz.jei.gui.input.handlers.FocusInputHandler;
import mezz.jei.gui.input.handlers.GlobalInputHandler;
import mezz.jei.gui.input.handlers.GuiAreaInputHandler;
import mezz.jei.gui.input.handlers.UserInputRouter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.CheatUtil;
import net.minecraft.core.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class JeiGuiStarter {
    private static final Logger LOGGER = LogManager.getLogger();

    public static JeiEventHandlers start(IRuntimeRegistration registration) {
        LOGGER.info("Starting JEI GUI");
        LoggedTimer timer = new LoggedTimer();

        IConnectionToServer serverConnection = Internal.getServerConnection();
        Textures textures = Internal.getTextures();
        IInternalKeyMappings keyMappings = Internal.getKeyMappings();

        IScreenHelper screenHelper = registration.getScreenHelper();
        IRecipeTransferManager recipeTransferManager = registration.getRecipeTransferManager();
        IRecipeManager recipeManager = registration.getRecipeManager();
        IIngredientVisibility ingredientVisibility = registration.getIngredientVisibility();
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IEditModeConfig editModeConfig = registration.getEditModeConfig();

        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IColorHelper colorHelper = jeiHelpers.getColorHelper();
        IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
        IFocusFactory focusFactory = jeiHelpers.getFocusFactory();

        IFilterTextSource filterTextSource = new FilterTextSource();

        timer.start("Building ingredient list");
        NonNullList<IListElement<?>> ingredientList = IngredientListElementFactory.createBaseList(ingredientManager);
        timer.stop();

        timer.start("Building ingredient filter");
        GuiConfigData configData = GuiConfigData.create();

        ModNameSortingConfig modNameSortingConfig = configData.modNameSortingConfig();
        IngredientTypeSortingConfig ingredientTypeSortingConfig = configData.ingredientTypeSortingConfig();
        IClientToggleState toggleState = Internal.getClientToggleState();
        IBookmarkConfig bookmarkConfig = configData.bookmarkConfig();

        IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
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
            ingredientManager,
            ingredientSorter,
            ingredientList,
            modIdHelper,
            ingredientVisibility,
            colorHelper
        );
        ingredientManager.registerIngredientListener(ingredientFilter);
        ingredientVisibility.registerListener(ingredientFilter::onIngredientVisibilityChanged);
        timer.stop();

        IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, filterTextSource);
        registration.setIngredientFilter(ingredientFilterApi);

        CheatUtil cheatUtil = new CheatUtil(ingredientManager);
        IngredientListOverlay ingredientListOverlay = OverlayHelper.createIngredientListOverlay(
            ingredientManager,
            screenHelper,
            ingredientFilter,
            filterTextSource,
            modIdHelper,
            keyMappings,
            ingredientListConfig,
            clientConfig,
            toggleState,
            editModeConfig,
            serverConnection,
            ingredientFilterConfig,
            textures,
            colorHelper,
            cheatUtil
        );
        registration.setIngredientListOverlay(ingredientListOverlay);

        BookmarkList bookmarkList = new BookmarkList(ingredientManager, bookmarkConfig, clientConfig);
        bookmarkConfig.loadBookmarks(ingredientManager, bookmarkList);

        BookmarkOverlay bookmarkOverlay = OverlayHelper.createBookmarkOverlay(
            ingredientManager,
            screenHelper,
            bookmarkList,
            modIdHelper,
            keyMappings,
            bookmarkListConfig,
            editModeConfig,
            ingredientFilterConfig,
            clientConfig,
            toggleState,
            serverConnection,
            textures,
            colorHelper,
            cheatUtil
        );
        registration.setBookmarkOverlay(bookmarkOverlay);

        GuiEventHandler guiEventHandler = new GuiEventHandler(
            screenHelper,
            bookmarkOverlay,
            ingredientListOverlay
        );

        RecipesGui recipesGui = new RecipesGui(
            recipeManager,
            recipeTransferManager,
            ingredientManager,
            modIdHelper,
            clientConfig,
            textures,
            keyMappings,
            focusFactory
        );
        registration.setRecipesGui(recipesGui);

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
            new EditInputHandler(recipeFocusSource, toggleState, editModeConfig),
            ingredientListOverlay.createInputHandler(),
            bookmarkOverlay.createInputHandler(),
            new FocusInputHandler(recipeFocusSource, recipesGui, focusFactory),
            new BookmarkInputHandler(recipeFocusSource, bookmarkList),
            new GlobalInputHandler(toggleState),
            new GuiAreaInputHandler(screenHelper, recipesGui, focusFactory)
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
