package mezz.jei.gui.startup;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IBookmarkManager;
import mezz.jei.api.runtime.IEditModeConfig;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IClientConfigs;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.common.transfer.RecipeTransferService;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.LoggedTimer;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.BookmarkManager;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.config.ILookupHistoryConfig;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.JeiGuiSortingConfigData;
import mezz.jei.gui.config.ModNameSortingConfig;
import mezz.jei.gui.events.GuiEventHandler;
import mezz.jei.gui.filter.FilterTextSource;
import mezz.jei.gui.filter.IFilterTextSource;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientFilterApi;
import mezz.jei.gui.ingredients.IngredientListElementFactory;
import mezz.jei.gui.ingredients.IngredientSorter;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.history.LookupHistory;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.util.FocusUtil;
import net.mezzdev.config.api.value.change.IConfigValueBatchChangeListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class JeiGuiStarter {
	private static final Logger LOGGER = LogManager.getLogger();

	public static JeiEventHandlers start(
		IRuntimeRegistration registration,
		JeiGuiSortingConfigData sortingConfigData
	) {
		LOGGER.info("Starting JEI GUI");
		LoggedTimer timer = new LoggedTimer();

		IConnectionToServer serverConnection = Internal.getServerConnection();
		Textures textures = Internal.getTextures();
		IInternalKeyMappings keyMappings = Internal.getKeyMappings();

		IScreenHelper screenHelper = registration.getScreenHelper();
		IRecipeTransferManager recipeTransferManager = registration.getRecipeTransferManager();
		RecipeTransferService recipeTransferService = new RecipeTransferService(recipeTransferManager);
		IRecipeManager recipeManager = registration.getRecipeManager();
		IIngredientManager ingredientManager = registration.getIngredientManager();
		IEditModeConfig editModeConfig = registration.getEditModeConfig();
		ISearchStorageBuilderFactory searchStorageBuilderFactory = registration.getSearchStorageBuilderFactory();

		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		IIngredientVisibility ingredientVisibility = jeiHelpers.getIngredientVisibility();
		IColorHelper colorHelper = jeiHelpers.getColorHelper();
		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();
		IFocusFactory focusFactory = jeiHelpers.getFocusFactory();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		IFilterTextSource filterTextSource = new FilterTextSource();
		Minecraft minecraft = Minecraft.getInstance();
		JeiGuiColors.onResourceManagerReload(minecraft.getResourceManager());
		ClientLevel level = minecraft.level;
		ErrorUtil.checkNotNull(level, "minecraft.level");
		RegistryAccess registryAccess = level.registryAccess();

		IClientConfigs jeiClientConfigs = Internal.getClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		registerJeiRestartingConfigListeners(jeiClientConfigs);
		IIngredientGridConfig ingredientListConfig = jeiClientConfigs.getIngredientListConfig();
		IIngredientGridConfig bookmarkListConfig = jeiClientConfigs.getBookmarkListConfig();
		IIngredientFilterConfig ingredientFilterConfig = jeiClientConfigs.getIngredientFilterConfig();

		timer.start("Building ingredient list");
		List<IListElementInfo<?>> ingredientList = IngredientListElementFactory.createBaseList(ingredientManager, ingredientFilterConfig, modIdHelper);
		timer.stop();

		timer.start("Building ingredient filter");
		GuiConfigData configData = GuiConfigData.create(sortingConfigData);

		ModNameSortingConfig modNameSortingConfig = configData.modNameSortingConfig();
		IngredientTypeSortingConfig ingredientTypeSortingConfig = configData.ingredientTypeSortingConfig();
		IWorldConfig worldConfig = Internal.getWorldConfig();
		IBookmarkConfig bookmarkConfig = configData.bookmarkConfig();
		ILookupHistoryConfig lookupHistoryConfig = configData.lookupHistoryConfig();

		Function<List<IListElementInfo<?>>, Comparator<IListElement<?>>> sortIndexUpdater = ingredients -> IngredientSorter.sortIngredients(
			clientConfig,
			modNameSortingConfig,
			ingredientTypeSortingConfig,
			ingredientManager,
			ingredients
		);

		IngredientFilter ingredientFilter = new IngredientFilter(
			filterTextSource,
			clientConfig,
			ingredientFilterConfig,
			ingredientManager,
			sortIndexUpdater,
			ingredientList,
			modIdHelper,
			ingredientVisibility,
			ingredientTypeSortingConfig,
			colorHelper,
			searchStorageBuilderFactory,
			worldConfig
		);
		jeiClientConfigs.registerRuntimeListenerRemoval(
			modNameSortingConfig.addChangeListener(ingredientFilter::onIngredientSortOrderConfigChanged)
		);
		jeiClientConfigs.registerRuntimeListenerRemoval(
			ingredientTypeSortingConfig.addChangeListener(ingredientFilter::onIngredientTypeSortOrderConfigChanged)
		);
		ingredientManager.registerIngredientListener(ingredientFilter);
		ingredientVisibility.registerListener(ingredientFilter);
		timer.stop();

		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, filterTextSource);
		registration.setIngredientFilter(ingredientFilterApi);

		LookupHistory lookupHistory = new LookupHistory(
			recipeManager,
			ingredientManager,
			focusFactory,
			guiHelper,
			clientConfig::getMaxLookupHistoryIngredients,
			lookupHistoryConfig,
			recipeTransferService
		);

		IngredientListOverlay ingredientListOverlay = OverlayHelper.createIngredientListOverlay(
			ingredientManager,
			screenHelper,
			ingredientFilter,
			lookupHistory,
			filterTextSource,
			keyMappings,
			ingredientListConfig,
			clientConfig,
			worldConfig,
			serverConnection,
			ingredientFilterConfig,
			textures,
			colorHelper,
			ingredientFilter
		);
		registration.setIngredientListOverlay(ingredientListOverlay);

		BookmarkList bookmarkList = new BookmarkList(recipeManager, focusFactory, ingredientManager, registryAccess, bookmarkConfig, clientConfig, guiHelper);
		bookmarkConfig.loadBookmarks(recipeManager, focusFactory, guiHelper, ingredientManager, registryAccess, bookmarkList, recipeTransferService);
		IBookmarkManager bookmarkManager = new BookmarkManager(bookmarkList, recipeManager, focusFactory, ingredientManager, recipeTransferService, guiHelper);
		registration.setBookmarkManager(bookmarkManager);

		BookmarkOverlay bookmarkOverlay = OverlayHelper.createBookmarkOverlay(
			ingredientManager,
			screenHelper,
			bookmarkList,
			recipeTransferService,
			lookupHistory,
			keyMappings,
			bookmarkListConfig,
			ingredientFilterConfig,
			clientConfig,
			worldConfig,
			serverConnection,
			textures,
			colorHelper
		);
		registration.setBookmarkOverlay(bookmarkOverlay);

		FocusUtil focusUtil = new FocusUtil(focusFactory, clientConfig, ingredientManager);

		RecipesGui recipesGui = new RecipesGui(
			recipeManager,
			ingredientManager,
			recipeTransferService,
			keyMappings,
			focusFactory,
			guiHelper,
			bookmarkList,
			lookupHistory,
			focusUtil,
			ingredientListOverlay
		);
		registration.setRecipesGui(recipesGui);
		JeiInputSetup input = JeiInputSetup.create(
			ingredientListOverlay,
			bookmarkOverlay,
			recipesGui,
			bookmarkList,
			bookmarkManager,
			screenHelper,
			ingredientManager,
			focusFactory,
			focusUtil,
			clientConfig,
			worldConfig,
			editModeConfig,
			serverConnection,
			keyMappings
		);
		GuiEventHandler guiEventHandler = new GuiEventHandler(
			screenHelper,
			bookmarkOverlay,
			ingredientListOverlay,
			input.scene()
		);
		ResourceReloadHandler resourceReloadHandler = new ResourceReloadHandler(
			ingredientListOverlay,
			ingredientFilter
		);

		return new JeiEventHandlers(
			guiEventHandler,
			input.handler(),
			resourceReloadHandler
		);
	}

	private static void registerJeiRestartingConfigListeners(IClientConfigs clientConfigs) {
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IConfigValueBatchChangeListener restartJei = changes -> Internal.restartJei();
		clientConfigs.registerRuntimeListenerRemoval(
			clientConfig.showTagRecipesEnabled().addBatchListener(restartJei)
		);
		clientConfigs.registerRuntimeListenerRemoval(
			clientConfig.showHiddenIngredients().addBatchListener(restartJei)
		);
	}
}
