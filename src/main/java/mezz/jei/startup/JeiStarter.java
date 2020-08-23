package mezz.jei.startup;

import java.util.List;
import java.util.Map;

import mezz.jei.gui.GuiContainerHandlers;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.GridAlignment;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.textures.Textures;
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
import mezz.jei.recipes.RecipeManager;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;

public class JeiStarter {
	private boolean started;

	public void start(
		List<IModPlugin> plugins,
		Textures textures,
		ClientConfig config,
		IEditModeConfig editModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IWorldConfig worldConfig,
		BookmarkConfig bookmarkConfig,
		IModIdHelper modIdHelper
	) {
		ErrorUtil.checkNotEmpty(plugins, "plugins");
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		boolean debugMode = config.isDebugModeEnabled();
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins);
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);
		PluginLoader pluginLoader = new PluginLoader(plugins, vanillaPlugin, textures, editModeConfig, ingredientFilterConfig, bookmarkConfig, modIdHelper, debugMode);
		GuiHandlerRegistration guiHandlerRegistration = pluginLoader.getGuiHandlerRegistration();
		IngredientManager ingredientManager = pluginLoader.getIngredientManager();
		IngredientFilter ingredientFilter = pluginLoader.getIngredientFilter();
		BookmarkList bookmarkList = pluginLoader.getBookmarkList();
		RecipeManager recipeManager = pluginLoader.getRecipeManager();
		RecipeTransferRegistration recipeTransferRegistration = pluginLoader.getRecipeTransferRegistration();
		RecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferRegistration.getRecipeTransferHandlers());

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		GuiContainerHandlers guiContainerHandlers = guiHandlerRegistration.getGuiContainerHandlers();
		List<IGlobalGuiHandler> globalGuiHandlers = guiHandlerRegistration.getGlobalGuiHandlers();
		Map<Class<?>, IScreenHandler<?>> guiScreenHandlers = guiHandlerRegistration.getGuiScreenHandlers();
		Map<Class<?>, IGhostIngredientHandler<?>> ghostIngredientHandlers = guiHandlerRegistration.getGhostIngredientHandlers();
		GuiScreenHelper guiScreenHelper = new GuiScreenHelper(ingredientManager, globalGuiHandlers, guiContainerHandlers, ghostIngredientHandlers, guiScreenHandlers);
		IngredientGridWithNavigation ingredientListGrid = new IngredientGridWithNavigation(ingredientFilter, worldConfig, guiScreenHelper, editModeConfig, ingredientFilterConfig, worldConfig, GridAlignment.LEFT);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(ingredientFilter, ingredientManager, guiScreenHelper, ingredientListGrid, worldConfig);

		IngredientGridWithNavigation bookmarkListGrid = new IngredientGridWithNavigation(bookmarkList, () -> "", guiScreenHelper, editModeConfig, ingredientFilterConfig, worldConfig, GridAlignment.RIGHT);
		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, textures, bookmarkListGrid, worldConfig);
		RecipesGui recipesGui = new RecipesGui(recipeManager, recipeTransferManager, ingredientManager, modIdHelper);
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
		totalTime.stop();
	}

	public boolean hasStarted() {
		return started;
	}
}
