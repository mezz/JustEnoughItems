package mezz.jei.startup;

import java.util.List;
import java.util.Map;

import net.minecraft.util.NonNullList;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGlobalGuiHandler;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.config.Config;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.InputHandler;
import mezz.jei.load.PluginLoader;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;

public class JeiStarter {
	private boolean started;

	public void start(List<IModPlugin> plugins, Textures textures) {
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		IModIdHelper modIdHelper = ForgeModIdHelper.getInstance();
		ErrorUtil.setModIdHelper(modIdHelper);
		PluginLoader.registerModInfo(plugins, modIdHelper);

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();

		PluginLoader.registerItemSubtypes(plugins, subtypeRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		stackHelper.enableUidCache();
		Internal.setStackHelper(stackHelper);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		ModIngredientRegistration modIngredientRegistry = PluginLoader.registerIngredients(plugins);
		PluginLoader.registerIngredientAliases(plugins, modIngredientRegistry);
		IngredientRegistry ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist);
		Internal.setIngredientRegistry(ingredientRegistry);

		GuiHelper guiHelper = new GuiHelper(ingredientRegistry, textures);
		JeiHelpers jeiHelpers = new JeiHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper);
		Internal.setHelpers(jeiHelpers);

		ModRegistry modRegistry = new ModRegistry(jeiHelpers, ingredientRegistry);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Registering recipe categories");
		PluginLoader.registerCategories(plugins, modRegistry);
		timer.stop();

		timer.start("Registering mod plugins");
		PluginLoader.registerPlugins(plugins, modRegistry);
		timer.stop();

		timer.start("Building recipe registry");
		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry(ingredientRegistry);
		timer.stop();

		timer.start("Building ingredient list");
		NonNullList<IIngredientListElement> ingredientList = IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper);
		timer.stop();

		timer.start("Building ingredient filter");
		IngredientFilter ingredientFilter = new IngredientFilter(blacklist);
		ingredientFilter.addIngredients(ingredientList);
		Internal.setIngredientFilter(ingredientFilter);
		timer.stop();

		timer.start("Building bookmarks");
		BookmarkList bookmarkList = new BookmarkList(ingredientRegistry);
		bookmarkList.loadBookmarks();
		timer.stop();

		timer.start("Building runtime");
		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
		List<IGlobalGuiHandler> globalGuiHandlers = modRegistry.getGlobalGuiHandlers();
		Map<Class, IGuiScreenHandler> guiScreenHandlers = modRegistry.getGuiScreenHandlers();
		ListMultiMap<Class, IGhostIngredientHandler> ghostIngredientHandlers = modRegistry.getGhostIngredientHandlers();
		GuiScreenHelper guiScreenHelper = new GuiScreenHelper(ingredientRegistry, globalGuiHandlers, advancedGuiHandlers, ghostIngredientHandlers, guiScreenHandlers);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(ingredientFilter, ingredientRegistry, guiScreenHelper);

		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, jeiHelpers.getGuiHelper(), guiScreenHelper, ingredientRegistry);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry, ingredientRegistry);
		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, ingredientListOverlay, bookmarkOverlay, recipesGui, ingredientFilter);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		stackHelper.disableUidCache();

		PluginLoader.sendRuntime(plugins, jeiRuntime);

		// Some mods insist on adding ingredients at runtime, so we cannot optimize memory usage earlier than that.
		if (Config.isOptimizeMemoryUsage()) {
			timer.start("Optimizing memory usage");
			ingredientFilter.trimToSize();
			timer.stop();
		}

		LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper);
		leftAreaDispatcher.addContent(bookmarkOverlay);

		GuiEventHandler guiEventHandler = new GuiEventHandler(guiScreenHelper, leftAreaDispatcher, ingredientListOverlay, recipeRegistry);
		Internal.setGuiEventHandler(guiEventHandler);
		InputHandler inputHandler = new InputHandler(jeiRuntime, ingredientRegistry, ingredientListOverlay, guiScreenHelper, leftAreaDispatcher, bookmarkList);
		Internal.setInputHandler(inputHandler);

		Config.checkForModNameFormatOverride();

		started = true;
		totalTime.stop();
	}

	public boolean hasStarted() {
		return started;
	}

}
