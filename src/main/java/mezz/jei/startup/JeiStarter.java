package mezz.jei.startup;

import com.google.common.base.Stopwatch;
import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IGuiScreenHandler;
import mezz.jei.config.Config;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
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
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.ProgressManager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JeiStarter {
	private boolean started;

	public void start(List<IModPlugin> plugins) {
		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();

		registerItemSubtypes(plugins, subtypeRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		stackHelper.enableUidCache();
		Internal.setStackHelper(stackHelper);

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		ModIngredientRegistration modIngredientRegistry = registerIngredients(plugins);
		IngredientRegistry ingredientRegistry = modIngredientRegistry.createIngredientRegistry(ForgeModIdHelper.getInstance(), blacklist);
		Internal.setIngredientRegistry(ingredientRegistry);

		JeiHelpers jeiHelpers = new JeiHelpers(ingredientRegistry, blacklist, stackHelper);
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
		NonNullList<IIngredientListElement> ingredientList = IngredientListElementFactory.createBaseList(ingredientRegistry, ForgeModIdHelper.getInstance());
		timer.stop();

		timer.start("Building ingredient filter");
		IngredientFilter ingredientFilter = new IngredientFilter(blacklist);
		ingredientFilter.addIngredients(ingredientList);
		Internal.setIngredientFilter(ingredientFilter);
		timer.stop();

		timer.start("Building runtime");
		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
		Map<Class, IGuiScreenHandler> guiScreenHandlers = modRegistry.getGuiScreenHandlers();
		Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = modRegistry.getGhostIngredientHandlers();
		IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(ingredientFilter);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry);
		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, ingredientListOverlay, recipesGui, ingredientRegistry, advancedGuiHandlers, guiScreenHandlers, ghostIngredientHandlers, ingredientFilter);
		Internal.setRuntime(jeiRuntime);
		timer.stop();

		stackHelper.disableUidCache();

		sendRuntime(plugins, jeiRuntime);

		GuiEventHandler guiEventHandler = new GuiEventHandler(ingredientListOverlay, recipeRegistry);
		Internal.setGuiEventHandler(guiEventHandler);
		InputHandler inputHandler = new InputHandler(jeiRuntime, ingredientListOverlay);
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
				if (VanillaPlugin.class.isInstance(plugin)) {
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
