package mezz.jei.common.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.common.Internal;
import mezz.jei.common.bookmarks.BookmarkList;
import mezz.jei.common.filter.FilterTextSource;
import mezz.jei.common.filter.IFilterTextSource;
import mezz.jei.common.gui.GuiEventHandler;
import mezz.jei.common.gui.GuiScreenHelper;
import mezz.jei.common.gui.overlay.IngredientListOverlay;
import mezz.jei.common.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.common.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.common.gui.recipes.RecipesGui;
import mezz.jei.common.helpers.ModIdHelper;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.ingredients.IngredientFilter;
import mezz.jei.common.ingredients.IngredientFilterApi;
import mezz.jei.common.ingredients.IngredientSorter;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.input.ClientInputHandler;
import mezz.jei.common.input.CombinedRecipeFocusSource;
import mezz.jei.common.input.GuiContainerWrapper;
import mezz.jei.common.input.ICharTypedHandler;
import mezz.jei.common.input.handlers.BookmarkInputHandler;
import mezz.jei.common.input.handlers.CombinedInputHandler;
import mezz.jei.common.input.handlers.EditInputHandler;
import mezz.jei.common.input.handlers.FocusInputHandler;
import mezz.jei.common.input.handlers.GlobalInputHandler;
import mezz.jei.common.input.handlers.GuiAreaInputHandler;
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
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins);
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
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

		RegisteredIngredients registeredIngredients = pluginLoader.getRegisteredIngredients();

		IngredientFilter ingredientFilter = pluginLoader.getIngredientFilter();

		BookmarkList bookmarkList = pluginLoader.createBookmarkList(configData.bookmarkConfig());
		RecipeManager recipeManager = pluginLoader.createRecipeManager(plugins, vanillaPlugin, configData.recipeCategorySortingConfig(), modIdHelper);
		ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers =
			pluginLoader.createRecipeTransferHandlers(plugins);
		RecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		GuiScreenHelper guiScreenHelper = pluginLoader.createGuiScreenHelper(plugins);

		IngredientListOverlay ingredientListOverlay = OverlayHelper.createIngredientListOverlay(
			data,
			registeredIngredients,
			guiScreenHelper,
			ingredientFilter,
			filterTextSource,
			modIdHelper
		);

		BookmarkOverlay bookmarkOverlay = OverlayHelper.createBookmarkOverlay(
			data,
			registeredIngredients,
			guiScreenHelper,
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
			jeiHelpers
		);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper, bookmarkOverlay);

		GuiEventHandler guiEventHandler = new GuiEventHandler(guiScreenHelper, leftAreaDispatcher, ingredientListOverlay);

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
			new EditInputHandler(recipeFocusSource, registeredIngredients, ingredientFilter, configData.worldConfig(), configData.editModeConfig()),
			ingredientListOverlay.createInputHandler(),
			leftAreaDispatcher.createInputHandler(),
			new FocusInputHandler(recipeFocusSource, recipesGui),
			new BookmarkInputHandler(recipeFocusSource, bookmarkList),
			new GlobalInputHandler(configData.worldConfig()),
			new GuiAreaInputHandler(registeredIngredients, guiScreenHelper, recipesGui)
		);
		ClientInputHandler clientInputHandler = new ClientInputHandler(charTypedHandlers, userInputHandler, data.keyBindings());

		// This needs to be run after all of the "Ingredients are being added at runtime" items.
		ingredientSorter.doPreSort(ingredientFilter, registeredIngredients);

		totalTime.stop();

		return new JeiEventHandlers(
			guiEventHandler,
			clientInputHandler
		);
	}

}
