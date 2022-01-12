package mezz.jei.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
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
import mezz.jei.input.CombinedRecipeFocusSource;
import mezz.jei.input.GuiContainerWrapper;
import mezz.jei.input.InputEventHandler;
import mezz.jei.input.mouse.ICharTypedHandler;
import mezz.jei.input.mouse.handlers.BookmarkInputHandler;
import mezz.jei.input.mouse.handlers.EditInputHandler;
import mezz.jei.input.mouse.handlers.FocusInputHandler;
import mezz.jei.input.mouse.handlers.GlobalInputHandler;
import mezz.jei.input.mouse.handlers.CombinedInputHandler;
import mezz.jei.input.mouse.handlers.GuiAreaInputHandler;
import mezz.jei.load.PluginCaller;
import mezz.jei.load.PluginHelper;
import mezz.jei.load.PluginLoader;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.recipes.RecipeTransferManager;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;
import net.minecraft.resources.ResourceLocation;

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

		Internal.setTextures(textures);

		boolean debugMode = clientConfig.isDebugModeEnabled();
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins);
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);
		PluginLoader pluginLoader = new PluginLoader(plugins, textures, clientConfig, modIdHelper, debugMode);
		IngredientManager ingredientManager = pluginLoader.getIngredientManager();
		IngredientFilter ingredientFilter = pluginLoader.createIngredientFilter(ingredientSorter, worldConfig, editModeConfig, ingredientFilterConfig);
		BookmarkList bookmarkList = pluginLoader.createBookmarkList(bookmarkConfig);
		IRecipeManager recipeManager = pluginLoader.createRecipeManager(plugins, vanillaPlugin, recipeCategorySortingConfig);
		ImmutableTable<Class<?>, ResourceLocation, IRecipeTransferHandler<?, ?>> recipeTransferHandlers = pluginLoader.createRecipeTransferHandlers(plugins);
		RecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		GuiScreenHelper guiScreenHelper = pluginLoader.createGuiScreenHelper(plugins);
		RecipesGui recipesGui = new RecipesGui(recipeManager, recipeTransferManager, modIdHelper, clientConfig);

		IngredientGrid ingredientListGrid = new IngredientGrid(ingredientManager, GridAlignment.LEFT, editModeConfig, ingredientFilterConfig, clientConfig, worldConfig, guiScreenHelper, recipesGui, modIdHelper);

		WeakIngredientGridSource weakIngredientGridSource = new WeakIngredientGridSource(ingredientFilter);
		IngredientGridWithNavigation ingredientListGridNavigation = new IngredientGridWithNavigation(weakIngredientGridSource, worldConfig, guiScreenHelper, ingredientListGrid, worldConfig, clientConfig);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(weakIngredientGridSource, ingredientManager, guiScreenHelper, ingredientListGridNavigation, clientConfig, worldConfig);

		IngredientGrid bookmarkListGrid = new IngredientGrid(ingredientManager, GridAlignment.RIGHT, editModeConfig, ingredientFilterConfig, clientConfig, worldConfig, guiScreenHelper, recipesGui, modIdHelper);
		IngredientGridWithNavigation bookmarkListGridNavigation = new IngredientGridWithNavigation(bookmarkList, () -> "", guiScreenHelper, bookmarkListGrid, worldConfig, clientConfig);
		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, textures, bookmarkListGridNavigation, clientConfig, worldConfig);

		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, worldConfig);

		JeiRuntime jeiRuntime = new JeiRuntime(recipeManager, ingredientListOverlay, bookmarkOverlay, recipesGui, ingredientFilterApi, ingredientManager);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper, bookmarkOverlay);

		GuiEventHandler guiEventHandler = new GuiEventHandler(guiScreenHelper, leftAreaDispatcher, ingredientListOverlay);
		Internal.setGuiEventHandler(guiEventHandler);

		CombinedRecipeFocusSource recipeFocusSource = new CombinedRecipeFocusSource(
			recipesGui,
			ingredientListOverlay,
			leftAreaDispatcher,
			new GuiContainerWrapper(guiScreenHelper)
		);

		List<ICharTypedHandler> charTypedHandlers = List.of(
			ingredientListOverlay
		);

		CombinedInputHandler userInputHandler = new CombinedInputHandler(
			new EditInputHandler(recipeFocusSource, ingredientManager, ingredientFilter, worldConfig, editModeConfig),
			ingredientListOverlay.createInputHandler(),
			leftAreaDispatcher.createInputHandler(),
			new FocusInputHandler(recipeFocusSource, recipesGui),
			new BookmarkInputHandler(recipeFocusSource, bookmarkList),
			new GlobalInputHandler(worldConfig),
			new GuiAreaInputHandler(guiScreenHelper, recipesGui)
		);
		InputEventHandler inputEventHandler = new InputEventHandler(charTypedHandlers, userInputHandler);
		Internal.setInputEventHandler(inputEventHandler);

		started = true;

		// This needs to be run after all of the "Ingredients are being added at runtime" items.
		ingredientSorter.doPreSort(ingredientFilter, ingredientManager);

		totalTime.stop();
	}

	public boolean hasStarted() {
		return started;
	}
}
