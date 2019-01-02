package mezz.jei.startup;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.progress.ProgressBar;
import net.minecraftforge.fml.common.progress.StartupProgressManager;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGlobalGuiHandler;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.GridAlignment;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientFilterApi;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.input.InputHandler;
import mezz.jei.load.ModRegistry;
import mezz.jei.load.PluginLoader;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LoggedTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JeiStarter {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private boolean started;

	public void start(List<IModPlugin> plugins, ClientConfig config, IHideModeConfig hideModeConfig, IModIdHelper modIdHelper) {
		ErrorUtil.checkNotEmpty(plugins, "plugins");
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		PluginLoader pluginLoader = new PluginLoader(plugins, config, hideModeConfig, modIdHelper);
		ModRegistry modRegistry = pluginLoader.getModRegistry();
		IngredientRegistry ingredientRegistry = pluginLoader.getIngredientRegistry();
		IngredientFilter ingredientFilter = pluginLoader.getIngredientFilter();
		JeiHelpers jeiHelpers = pluginLoader.getJeiHelpers();
		BookmarkList bookmarkList = pluginLoader.getBookmarkList();
		RecipeRegistry recipeRegistry = pluginLoader.getRecipeRegistry();

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
		List<IGlobalGuiHandler> globalGuiHandlers = modRegistry.getGlobalGuiHandlers();
		Map<Class, IGuiScreenHandler> guiScreenHandlers = modRegistry.getGuiScreenHandlers();
		Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = modRegistry.getGhostIngredientHandlers();
		GuiScreenHelper guiScreenHelper = new GuiScreenHelper(ingredientRegistry, globalGuiHandlers, advancedGuiHandlers, ghostIngredientHandlers, guiScreenHandlers);
		IngredientGridWithNavigation ingredientListGrid = new IngredientGridWithNavigation(ingredientFilter, config, guiScreenHelper, hideModeConfig, GridAlignment.LEFT);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(ingredientFilter, config, ingredientRegistry, guiScreenHelper, ingredientListGrid);

		IngredientGridWithNavigation bookmarkListGrid = new IngredientGridWithNavigation(ingredientFilter, () -> "", guiScreenHelper, hideModeConfig, GridAlignment.RIGHT);
		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, jeiHelpers.getGuiHelper(), bookmarkListGrid);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry, ingredientRegistry);
		IIngredientFilter ingredientFilterApi = new IngredientFilterApi(ingredientFilter);
		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, ingredientListOverlay, recipesGui, ingredientFilterApi);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		sendRuntime(plugins, jeiRuntime);

		LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper);
		leftAreaDispatcher.addContent(bookmarkOverlay);

		GuiEventHandler guiEventHandler = new GuiEventHandler(guiScreenHelper, leftAreaDispatcher, ingredientListOverlay, recipeRegistry);
		Internal.setGuiEventHandler(guiEventHandler);
		InputHandler inputHandler = new InputHandler(jeiRuntime, ingredientFilter, ingredientRegistry, ingredientListOverlay, hideModeConfig, guiScreenHelper, leftAreaDispatcher, bookmarkList);
		Internal.setInputHandler(inputHandler);

		started = true;
		totalTime.stop();
	}

	public boolean hasStarted() {
		return started;
	}

	private static void sendRuntime(List<IModPlugin> plugins, IJeiRuntime jeiRuntime) {
		try (ProgressBar progressBar = StartupProgressManager.start("Sending Runtime", plugins.size())) {
			Iterator<IModPlugin> iterator = plugins.iterator();
			while (iterator.hasNext()) {
				IModPlugin plugin = iterator.next();
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					progressBar.step(pluginUid.toString());
					long start_time = System.currentTimeMillis();
					LOGGER.debug("Sending runtime to plugin: {} ...", pluginUid);
					plugin.onRuntimeAvailable(jeiRuntime);
					long timeElapsedMs = System.currentTimeMillis() - start_time;
					if (timeElapsedMs > 100) {
						LOGGER.warn("Sending runtime to plugin: {} took {} ms", pluginUid, timeElapsedMs);
					}
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
					iterator.remove();
				}
			}
		}
	}

}
