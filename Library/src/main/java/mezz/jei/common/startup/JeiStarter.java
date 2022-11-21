package mezz.jei.common.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.filter.FilterTextSource;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.GuiEventHandler;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.gui.recipes.RecipesGui;
import mezz.jei.common.helpers.ModIdHelper;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.IngredientFilterApi;
import mezz.jei.common.ingredients.IngredientSorter;
import mezz.jei.common.input.ClientInputHandler;
import mezz.jei.common.input.CombinedRecipeFocusSource;
import mezz.jei.common.input.GuiContainerWrapper;
import mezz.jei.common.input.ICharTypedHandler;
import mezz.jei.common.input.handlers.BookmarkInputHandler;
import mezz.jei.common.input.handlers.DragRouter;
import mezz.jei.common.input.handlers.EditInputHandler;
import mezz.jei.common.input.handlers.FocusInputHandler;
import mezz.jei.common.input.handlers.GlobalInputHandler;
import mezz.jei.common.input.handlers.GuiAreaInputHandler;
import mezz.jei.common.input.handlers.UserInputRouter;
import mezz.jei.common.load.PluginCaller;
import mezz.jei.common.load.PluginHelper;
import mezz.jei.common.load.PluginLoader;
import mezz.jei.common.plugins.jei.JeiInternalPlugin;
import mezz.jei.common.plugins.vanilla.VanillaPlugin;
import mezz.jei.common.recipes.RecipeManager;
import mezz.jei.common.recipes.RecipeTransferManager;
import mezz.jei.common.runtime.JeiHelpers;
import mezz.jei.common.runtime.JeiRuntime;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.LoggedTimer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public final class JeiStarter {
	private final StartData data;

	public JeiStarter(StartData data) {
		ErrorUtil.checkNotEmpty(data.plugins(), "plugins");
		this.data = data;
	}

	public JeiEventHandlers start() {
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");
		List<IModPlugin> plugins = data.plugins();
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins)
			.orElseThrow(() -> new IllegalStateException("vanilla plugin not found"));
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins)
			.orElse(null);
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);

		ConfigData configData = data.configData();

		IIngredientSorter ingredientSorter = new IngredientSorter(
			configData.clientConfig(),
			configData.modNameSortingConfig(),
			configData.ingredientTypeSortingConfig()
		);

		IFilterTextSource filterTextSource = new FilterTextSource();
		IModIdHelper modIdHelper = new ModIdHelper(configData.clientConfig(), configData.modIdFormatConfig());
		PluginLoader pluginLoader = new PluginLoader(data, filterTextSource, modIdHelper, ingredientSorter);
		JeiHelpers jeiHelpers = pluginLoader.getJeiHelpers();

		IRegisteredIngredients registeredIngredients = pluginLoader.getRegisteredIngredients();

		IngredientFilter ingredientFilter = pluginLoader.getIngredientFilter();

		BookmarkList bookmarkList = pluginLoader.createBookmarkList(configData.bookmarkConfig());
		RecipeManager recipeManager = pluginLoader.createRecipeManager(plugins, vanillaPlugin, configData.recipeCategorySortingConfig(), modIdHelper);
		ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers =
			pluginLoader.createRecipeTransferHandlers(plugins);
		IRecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		IScreenHelper screenHelper = pluginLoader.createGuiScreenHelper(plugins);

		IngredientListOverlay ingredientListOverlay = OverlayHelper.createIngredientListOverlay(
			data,
			registeredIngredients,
			screenHelper,
			ingredientFilter,
			filterTextSource,
			modIdHelper
		);

		BookmarkOverlay bookmarkOverlay = OverlayHelper.createBookmarkOverlay(
			data,
			registeredIngredients,
			screenHelper,
			bookmarkList,
			modIdHelper
		);

		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter, filterTextSource);
		IIngredientManager ingredientManager = pluginLoader.getIngredientManager();
		IIngredientVisibility ingredientVisibility = pluginLoader.getIngredientVisibility();

		RecipesGui recipesGui = new RecipesGui(
			recipeManager,
			recipeTransferManager,
			registeredIngredients,
			modIdHelper,
			configData.clientConfig(),
			data.textures(),
			ingredientVisibility,
			data.keyBindings()
		);

		JeiRuntime jeiRuntime = new JeiRuntime(
			recipeManager,
			ingredientListOverlay,
			bookmarkOverlay,
			recipesGui,
			ingredientFilterApi,
			registeredIngredients,
			ingredientManager,
			ingredientVisibility,
			data.keyBindings(),
			jeiHelpers
		);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		GuiEventHandler guiEventHandler = new GuiEventHandler(screenHelper, bookmarkOverlay, ingredientListOverlay, configData.clientConfig());

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
			configData.clientConfig(),
			new EditInputHandler(recipeFocusSource, registeredIngredients, ingredientFilter, configData.worldConfig(), configData.editModeConfig()),
			ingredientListOverlay.createInputHandler(),
			bookmarkOverlay.createInputHandler(),
			new FocusInputHandler(recipeFocusSource, recipesGui),
			new BookmarkInputHandler(recipeFocusSource, bookmarkList),
			new GlobalInputHandler(configData.worldConfig()),
			new GuiAreaInputHandler(registeredIngredients, screenHelper, recipesGui)
		);

		DragRouter dragRouter = new DragRouter(
			ingredientListOverlay.createDragHandler()
		);
		ClientInputHandler clientInputHandler = new ClientInputHandler(charTypedHandlers, userInputRouter, dragRouter, data.keyBindings());

		// This needs to be run after all of the "Ingredients are being added at runtime" items.
		ingredientSorter.doPreSort(ingredientFilter, registeredIngredients);

		totalTime.stop();

		return new JeiEventHandlers(
			guiEventHandler,
			clientInputHandler
		);
	}

}
