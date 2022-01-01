package mezz.jei.startup;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.GridAlignment;
import mezz.jei.gui.overlay.IngredientGrid;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.WeakIngredientGridSource;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientFilterApi;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.input.InputHandler;
import mezz.jei.load.PluginCaller;
import mezz.jei.load.PluginHelper;
import mezz.jei.load.PluginLoader;
import mezz.jei.load.registration.GuiHandlerRegistration;
import mezz.jei.load.registration.RecipeTransferRegistration;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;

import java.util.List;

public class JeiStarter {
	private boolean started;

	public void start(
		List<IModPlugin> plugins,
		Textures textures,
		IClientConfig clientConfig,
		IEditModeConfig editModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		BookmarkConfig bookmarkConfig,
		IModIdHelper modIdHelper,
		RecipeCategorySortingConfig recipeCategorySortingConfig,
		IIngredientSorter ingredientSorter
	) {
		ErrorUtil.checkNotEmpty(plugins, "plugins");
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		boolean debugMode = clientConfig.isDebugModeEnabled();
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins);
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);
		PluginLoader pluginLoader = new PluginLoader(plugins, vanillaPlugin, textures, clientConfig, modIdHelper, debugMode);
		GuiHandlerRegistration guiHandlerRegistration = pluginLoader.getGuiHandlerRegistration();
		IngredientManager ingredientManager = pluginLoader.getIngredientManager();
		IngredientFilter ingredientFilter = pluginLoader.createIngredientFilter(ingredientSorter, worldConfig, editModeConfig, ingredientFilterConfig);
		BookmarkList bookmarkList = pluginLoader.createBookmarkList(bookmarkConfig);
		IRecipeManager recipeManager = pluginLoader.getRecipeManager(recipeCategorySortingConfig);
		RecipeTransferRegistration recipeTransferRegistration = pluginLoader.getRecipeTransferRegistration();
		RecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferRegistration.getRecipeTransferHandlers());

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		GuiScreenHelper guiScreenHelper = guiHandlerRegistration.createGuiScreenHelper(ingredientManager);
		RecipesGui recipesGui = new RecipesGui(recipeManager, recipeTransferManager, ingredientManager, modIdHelper, clientConfig);

		IngredientGrid ingredientListGrid = new IngredientGrid(GridAlignment.LEFT, editModeConfig, ingredientFilterConfig, clientConfig, worldConfig, guiScreenHelper, recipesGui);

		WeakIngredientGridSource weakIngredientGridSource = new WeakIngredientGridSource(ingredientFilter);
		IngredientGridWithNavigation ingredientListGridNavigation = new IngredientGridWithNavigation(weakIngredientGridSource, worldConfig, guiScreenHelper, ingredientListGrid, worldConfig);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(weakIngredientGridSource, ingredientManager, guiScreenHelper, ingredientListGridNavigation, clientConfig, worldConfig);

		IngredientGrid bookmarkListGrid = new IngredientGrid(GridAlignment.RIGHT, editModeConfig, ingredientFilterConfig, clientConfig, worldConfig, guiScreenHelper, recipesGui);
		IngredientGridWithNavigation bookmarkListGridNavigation = new IngredientGridWithNavigation(bookmarkList, () -> "", guiScreenHelper, bookmarkListGrid, worldConfig);
		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, textures, bookmarkListGridNavigation, clientConfig, worldConfig);

		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, worldConfig);

		JeiRuntime jeiRuntime = new JeiRuntime(recipeManager, ingredientListOverlay, bookmarkOverlay, recipesGui, ingredientFilterApi, ingredientManager);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper);
		leftAreaDispatcher.addContent(bookmarkOverlay);

		GuiEventHandler guiEventHandler = new GuiEventHandler(guiScreenHelper, leftAreaDispatcher, ingredientListOverlay);
		Internal.setGuiEventHandler(guiEventHandler);
		InputHandler inputHandler = new InputHandler(recipesGui, ingredientFilter, ingredientManager, ingredientListOverlay, editModeConfig, worldConfig, guiScreenHelper, leftAreaDispatcher, bookmarkList);
		Internal.setInputHandler(inputHandler);

		started = true;

		//This needs to be run after all of the "Ingredients are being added at runtime" items.
		ingredientSorter.doPreSort(ingredientFilter, ingredientManager);

		totalTime.stop();
	}

	public boolean hasStarted() {
		return started;
	}
}
