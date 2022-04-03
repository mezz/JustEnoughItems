package mezz.jei.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.forge.config.IngredientFilterConfig;
import mezz.jei.forge.config.JEIClientConfigs;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.events.GuiEventHandler;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.FilterTextSource;
import mezz.jei.gui.overlay.IFilterTextSource;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientFilterApi;
import mezz.jei.ingredients.IngredientVisibility;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.GuiContainerWrapper;
import mezz.jei.input.InputEventHandler;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.handlers.BookmarkInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.EditInputHandler;
import mezz.jei.input.mouse.handlers.FocusInputHandler;
import mezz.jei.input.mouse.handlers.GlobalInputHandler;
import mezz.jei.input.mouse.handlers.GuiAreaInputHandler;
import mezz.jei.load.PluginCaller;
import mezz.jei.load.PluginHelper;
import mezz.jei.load.PluginLoader;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.recipes.RecipeManager;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ErrorUtil;
import mezz.jei.common.util.LoggedTimer;

import java.util.List;

public final class JeiStarter {
	private final List<IModPlugin> plugins;
	private final Textures textures;
	private final JEIClientConfigs clientConfigs;
	private final IEditModeConfig editModeConfig;
	private final IWorldConfig worldConfig;
	private final BookmarkConfig bookmarkConfig;
	private final IModIdHelper modIdHelper;
	private final RecipeCategorySortingConfig recipeCategorySortingConfig;
	private final IIngredientSorter ingredientSorter;
	private final IConnectionToServer serverConnection;

	public JeiStarter(
		List<IModPlugin> plugins,
		Textures textures,
		JEIClientConfigs clientConfigs,
		IEditModeConfig editModeConfig,
		IWorldConfig worldConfig,
		IConnectionToServer serverConnection,
		BookmarkConfig bookmarkConfig,
		IModIdHelper modIdHelper,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IIngredientSorter ingredientSorter
	) {
		ErrorUtil.checkNotEmpty(plugins, "plugins");
		this.plugins = plugins;
		this.textures = textures;
		this.clientConfigs = clientConfigs;
		this.editModeConfig = editModeConfig;
		this.worldConfig = worldConfig;
		this.serverConnection = serverConnection;
		this.bookmarkConfig = bookmarkConfig;
		this.modIdHelper = modIdHelper;
		this.recipeCategorySortingConfig = recipeCategorySortingConfig;
		this.ingredientSorter = ingredientSorter;
	}

	public void start(RuntimeEventSubscriptions subscriptions) {
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins);
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);
		IClientConfig clientConfig = clientConfigs.getClientConfig();
		IngredientFilterConfig ingredientFilterConfig = clientConfigs.getFilterConfig();
		IFilterTextSource filterTextSource = new FilterTextSource();
		PluginLoader pluginLoader = new PluginLoader(
			plugins,
			textures,
			clientConfig,
			modIdHelper,
			ingredientSorter,
			ingredientFilterConfig,
			worldConfig,
			editModeConfig,
			filterTextSource,
			serverConnection
		);
		JeiHelpers jeiHelpers = pluginLoader.getJeiHelpers();

		RegisteredIngredients registeredIngredients = pluginLoader.getRegisteredIngredients();

		IngredientFilter ingredientFilter = pluginLoader.getIngredientFilter();
		ingredientFilter.register(subscriptions);

		BookmarkList bookmarkList = pluginLoader.createBookmarkList(bookmarkConfig);
		RecipeManager recipeManager = pluginLoader.createRecipeManager(plugins, vanillaPlugin, recipeCategorySortingConfig);
		ImmutableTable<Class<?>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers =
			pluginLoader.createRecipeTransferHandlers(plugins, recipeManager);
		RecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		GuiScreenHelper guiScreenHelper = pluginLoader.createGuiScreenHelper(plugins);

		IngredientGrid ingredientListGrid = new IngredientGrid(
			registeredIngredients,
			clientConfigs.getIngredientListConfig(),
			editModeConfig,
			ingredientFilterConfig,
			clientConfig,
			worldConfig,
			guiScreenHelper,
			modIdHelper,
			serverConnection
		);

		IngredientGridWithNavigation ingredientListGridNavigation = new IngredientGridWithNavigation(
			ingredientFilter,
			guiScreenHelper,
			ingredientListGrid,
			worldConfig,
			clientConfig,
			serverConnection,
			clientConfigs.getIngredientListConfig(),
			textures.getIngredientListBackground(),
			textures.getIngredientListSlotBackground()
		);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(
			ingredientFilter,
			filterTextSource,
			registeredIngredients,
			guiScreenHelper,
			ingredientListGridNavigation,
			clientConfig,
			worldConfig,
			serverConnection
		);

		IngredientGrid bookmarkListGrid = new IngredientGrid(
			registeredIngredients,
			clientConfigs.getBookmarkListConfig(),
			editModeConfig,
			ingredientFilterConfig,
			clientConfig,
			worldConfig,
			guiScreenHelper,
			modIdHelper,
			serverConnection
		);

		IngredientGridWithNavigation bookmarkListGridNavigation = new IngredientGridWithNavigation(
			bookmarkList,
			guiScreenHelper,
			bookmarkListGrid,
			worldConfig,
			clientConfig,
			serverConnection,
			clientConfigs.getBookmarkListConfig(),
			textures.getBookmarkListBackground(),
			textures.getBookmarkListSlotBackground()
		);
		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(
			bookmarkList,
			textures,
			bookmarkListGridNavigation,
			clientConfig,
			worldConfig,
			guiScreenHelper,
			serverConnection
		);

		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, filterTextSource);
		IIngredientManager ingredientManager = pluginLoader.getIngredientManager();
		IngredientVisibility ingredientVisibility = pluginLoader.getIngredientVisibility();

		RecipesGui recipesGui = new RecipesGui(recipeManager, recipeTransferManager, registeredIngredients, modIdHelper, clientConfig);

		JeiRuntime jeiRuntime = new JeiRuntime(
			recipeManager,
			ingredientListOverlay,
			bookmarkOverlay,
			recipesGui,
			ingredientFilterApi,
			registeredIngredients,
			ingredientManager,
			ingredientVisibility,
			jeiHelpers
		);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper, bookmarkOverlay);

		GuiEventHandler guiEventHandler = new GuiEventHandler(guiScreenHelper, leftAreaDispatcher, ingredientListOverlay);
		guiEventHandler.register(subscriptions);

		CombinedRecipeFocusSource recipeFocusSource = new CombinedRecipeFocusSource(
			recipesGui,
			ingredientListOverlay,
			leftAreaDispatcher,
			new GuiContainerWrapper(registeredIngredients, guiScreenHelper)
		);

		List<ICharTypedHandler> charTypedHandlers = List.of(
			ingredientListOverlay
		);

		CombinedInputHandler userInputHandler = new CombinedInputHandler(
			new EditInputHandler(recipeFocusSource, registeredIngredients, ingredientFilter, worldConfig, editModeConfig),
			ingredientListOverlay.createInputHandler(),
			leftAreaDispatcher.createInputHandler(),
			new FocusInputHandler(recipeFocusSource, recipesGui),
			new BookmarkInputHandler(recipeFocusSource, bookmarkList),
			new GlobalInputHandler(worldConfig),
			new GuiAreaInputHandler(registeredIngredients, guiScreenHelper, recipesGui)
		);
		InputEventHandler inputEventHandler = new InputEventHandler(charTypedHandlers, userInputHandler);
		inputEventHandler.register(subscriptions);

		// This needs to be run after all of the "Ingredients are being added at runtime" items.
		ingredientSorter.doPreSort(ingredientFilter, registeredIngredients);

		totalTime.stop();
	}
}
