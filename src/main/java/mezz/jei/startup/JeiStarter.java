package mezz.jei.startup;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mezz.jei.util.ErrorUtil;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraft.util.NonNullList;

import com.google.common.base.Stopwatch;
import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGlobalGuiHandler;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.bookmarks.BookmarkList;
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
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.util.Log;

public class JeiStarter {
	private boolean started;

	public void start(List<IModPlugin> plugins, Textures textures) {
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		IModIdHelper modIdHelper = ForgeModIdHelper.getInstance();
		ErrorUtil.setModIdHelper(modIdHelper);

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();

		registerItemSubtypes(plugins, subtypeRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		stackHelper.enableUidCache();
		Internal.setStackHelper(stackHelper);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		ModIngredientRegistration modIngredientRegistry = registerIngredients(plugins);
		IngredientRegistry ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist);
		Internal.setIngredientRegistry(ingredientRegistry);

		GuiHelper guiHelper = new GuiHelper(ingredientRegistry, textures);
		JeiHelpers jeiHelpers = new JeiHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper);
		Internal.setHelpers(jeiHelpers);

		ModRegistry modRegistry = new ModRegistry(jeiHelpers, ingredientRegistry);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Registering recipe categories");
		registerCategories(plugins, modRegistry);
		timer.stop();

		timer.start("Registering mod plugins");
		registerPlugins(plugins, modRegistry);
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
		Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = modRegistry.getGhostIngredientHandlers();
		GuiScreenHelper guiScreenHelper = new GuiScreenHelper(ingredientRegistry, globalGuiHandlers, advancedGuiHandlers, ghostIngredientHandlers, guiScreenHandlers);
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(ingredientFilter, ingredientRegistry, guiScreenHelper);

		BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, jeiHelpers.getGuiHelper(), guiScreenHelper);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry, ingredientRegistry);
		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, ingredientListOverlay, bookmarkOverlay, recipesGui, ingredientFilter);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		stackHelper.disableUidCache();

		sendRuntime(plugins, jeiRuntime);

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

	private static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering item subtypes", plugins.size());
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				plugin.registerItemSubtypes(subtypeRegistry);
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
		ProgressManager.pop(progressBar);
	}

	private static ModIngredientRegistration registerIngredients(List<IModPlugin> plugins) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients", plugins.size());
		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				plugin.registerIngredients(modIngredientRegistry);
			} catch (RuntimeException | LinkageError e) {
				if (plugin instanceof VanillaPlugin) {
					throw e;
				} else {
					Log.get().error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
					iterator.remove();
				}
			}
		}
		ProgressManager.pop(progressBar);

		return modIngredientRegistry;
	}

	private static void registerCategories(List<IModPlugin> plugins, ModRegistry modRegistry) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering categories", plugins.size());
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				long start_time = System.currentTimeMillis();
				Log.get().debug("Registering categories: {} ...", plugin.getClass().getName());
				plugin.registerCategories(modRegistry);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				Log.get().debug("Registered  categories: {} in {} ms", plugin.getClass().getName(), timeElapsedMs);
			} catch (AbstractMethodError ignored) {
				// legacy plugins do not implement registerCategories
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Failed to register mod categories: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
		ProgressManager.pop(progressBar);
	}

	private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering plugins", plugins.size());
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				long start_time = System.currentTimeMillis();
				Log.get().debug("Registering plugin: {} ...", plugin.getClass().getName());
				plugin.register(modRegistry);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				Log.get().debug("Registered  plugin: {} in {} ms", plugin.getClass().getName(), timeElapsedMs);
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
		ProgressManager.pop(progressBar);
	}

	private static void sendRuntime(List<IModPlugin> plugins, IJeiRuntime jeiRuntime) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Sending Runtime", plugins.size());
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				long start_time = System.currentTimeMillis();
				Log.get().debug("Sending runtime to plugin: {} ...", plugin.getClass().getName());
				plugin.onRuntimeAvailable(jeiRuntime);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				if (timeElapsedMs > 100) {
					Log.get().warn("Sending runtime to plugin: {} took {} ms", plugin.getClass().getName(), timeElapsedMs);
				}
			} catch (RuntimeException | LinkageError e) {
				Log.get().error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
		ProgressManager.pop(progressBar);
	}

	private static class LoggedTimer {
		private final Stopwatch stopWatch = Stopwatch.createUnstarted();
		private String message = "";

		public void start(String message) {
			this.message = message;
			Log.get().info("{}...", message);
			stopWatch.reset();
			stopWatch.start();
		}

		public void stop() {
			stopWatch.stop();
			Log.get().info("{} took {}", message, stopWatch);
		}
	}
}
