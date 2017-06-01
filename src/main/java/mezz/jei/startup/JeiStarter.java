package mezz.jei.startup;

import java.util.Iterator;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.gui.GuiEventHandler;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.ingredients.IngredientLookupMemory;
import mezz.jei.gui.overlay.ItemListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientInformation;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.util.Log;
import net.minecraftforge.fml.common.ProgressManager;

public class JeiStarter {
	private boolean started;

	public void start(List<IModPlugin> plugins, final boolean resourceReload) {
		long jeiStartTime = System.currentTimeMillis();

		Log.info("Starting JEI...");
		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();

		registerItemSubtypes(plugins, subtypeRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		stackHelper.enableUidCache();
		Internal.setStackHelper(stackHelper);

		IngredientRegistry ingredientRegistry = registerIngredients(plugins);
		Internal.setIngredientRegistry(ingredientRegistry);

		JeiHelpers jeiHelpers = new JeiHelpers(ingredientRegistry, stackHelper);
		Internal.setHelpers(jeiHelpers);

		ModRegistry modRegistry = new ModRegistry(jeiHelpers, ingredientRegistry);

		registerPlugins(plugins, modRegistry);

		Log.info("Building recipe registry...");
		long start_time = System.currentTimeMillis();
		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry(ingredientRegistry);
		Log.info("Built    recipe registry in {} ms", System.currentTimeMillis() - start_time);

		Log.info("Loading ingredient lookup history...");
		start_time = System.currentTimeMillis();
		Internal.setIngredientLookupMemory(new IngredientLookupMemory(recipeRegistry, ingredientRegistry));
		Log.info("Loaded  ingredient lookup history in {} ms", System.currentTimeMillis() - start_time);

		IngredientInformation.onStart(resourceReload);

		Log.info("Building ingredient list...");
		start_time = System.currentTimeMillis();
		List<IIngredientListElement> ingredientList = IngredientListElementFactory.createBaseList(ingredientRegistry, ForgeModIdHelper.getInstance());
		Log.info("Built    ingredient list in {} ms", System.currentTimeMillis() - start_time);

		Log.info("Building ingredient filter...");
		start_time = System.currentTimeMillis();
		IngredientFilter ingredientFilter = new IngredientFilter(jeiHelpers);
		ingredientFilter.addIngredients(ingredientList);
		Internal.setIngredientFilter(ingredientFilter);
		Log.info("Built    ingredient filter in {} ms", System.currentTimeMillis() - start_time);

		Log.info("Building runtime...");
		start_time = System.currentTimeMillis();
		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
		ItemListOverlay itemListOverlay = new ItemListOverlay(ingredientFilter, ingredientRegistry);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry);
		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, itemListOverlay, recipesGui, ingredientRegistry, advancedGuiHandlers);
		Internal.setRuntime(jeiRuntime);
		Log.info("Built    runtime in {} ms", System.currentTimeMillis() - start_time);

		stackHelper.disableUidCache();

		sendRuntime(plugins, jeiRuntime);

		GuiEventHandler guiEventHandler = new GuiEventHandler(jeiRuntime);
		Internal.setGuiEventHandler(guiEventHandler);

		started = true;
		Log.info("Finished Starting JEI in {} ms", System.currentTimeMillis() - jeiStartTime);
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
			} catch (RuntimeException e) {
				Log.error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
		ProgressManager.pop(progressBar);
	}

	private static IngredientRegistry registerIngredients(List<IModPlugin> plugins) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients", plugins.size());
		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				plugin.registerIngredients(modIngredientRegistry);
			} catch (RuntimeException e) {
				if (VanillaPlugin.class.isInstance(plugin)) {
					throw e;
				} else {
					Log.error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
					iterator.remove();
				}
			} catch (LinkageError e) {
				if (VanillaPlugin.class.isInstance(plugin)) {
					throw e;
				} else {
					Log.error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
					iterator.remove();
				}
			}
		}
		ProgressManager.pop(progressBar);

		return modIngredientRegistry.createIngredientRegistry();
	}

	private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
		ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering plugins", plugins.size());
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				progressBar.step(plugin.getClass().getName());
				long start_time = System.currentTimeMillis();
				Log.info("Registering plugin: {} ...", plugin.getClass().getName());
				plugin.register(modRegistry);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				Log.info("Registered  plugin: {} in {} ms", plugin.getClass().getName(), timeElapsedMs);
			} catch (RuntimeException e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Failed to register mod plugin: {}", plugin.getClass(), e);
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
				Log.info("Sending runtime to plugin: {} ...", plugin.getClass().getName());
				plugin.onRuntimeAvailable(jeiRuntime);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				if (timeElapsedMs > 100) {
					Log.warning("Sending runtime to plugin: {} took {} ms", plugin.getClass().getName(), timeElapsedMs);
				}
			} catch (RuntimeException e) {
				Log.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
		ProgressManager.pop(progressBar);
	}
}
