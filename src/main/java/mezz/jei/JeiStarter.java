package mezz.jei;

import java.util.Iterator;
import java.util.List;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.util.Log;
import mezz.jei.util.ModIdUtil;
import mezz.jei.util.ModRegistry;
import mezz.jei.util.StackHelper;

public class JeiStarter {
	public static JeiRuntime startJEI(List<IModPlugin> plugins) {
		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();

		registerItemSubtypes(plugins, subtypeRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		stackHelper.enableUidCache();
		Internal.setStackHelper(stackHelper);

		IngredientRegistry ingredientRegistry = registerIngredients(plugins);
		Internal.setIngredientRegistry(ingredientRegistry);

		JeiHelpers jeiHelpers = new JeiHelpers(ingredientRegistry, stackHelper, subtypeRegistry);
		Internal.setHelpers(jeiHelpers);

		ModIdUtil modIdUtil = Internal.getModIdUtil();
		ItemRegistry itemRegistry = new ItemRegistry(ingredientRegistry, modIdUtil);

		ModRegistry modRegistry = new ModRegistry(jeiHelpers, itemRegistry, ingredientRegistry);

		registerPlugins(plugins, modRegistry);

		Log.info("Building recipe registry...");
		long start_time = System.currentTimeMillis();
		RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry(stackHelper, ingredientRegistry);
		Log.info("Built    recipe registry in {} ms", System.currentTimeMillis() - start_time);

		ItemFilter itemFilter = new ItemFilter();
		itemFilter.build(ingredientRegistry, jeiHelpers);

		Log.info("Building runtime...");
		start_time = System.currentTimeMillis();
		List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter, advancedGuiHandlers, ingredientRegistry);
		RecipesGui recipesGui = new RecipesGui(recipeRegistry);
		JeiRuntime jeiRuntime = new JeiRuntime(recipeRegistry, itemListOverlay, recipesGui, ingredientRegistry);
		Internal.setRuntime(jeiRuntime);
		Log.info("Built    runtime in {} ms", System.currentTimeMillis() - start_time);

		stackHelper.disableUidCache();

		sendRuntime(plugins, jeiRuntime);

		return jeiRuntime;
	}

	private static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.registerItemSubtypes(subtypeRegistry);
			} catch (RuntimeException e) {
				Log.error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (AbstractMethodError ignored) {
				// legacy mod plugins do not have registerItemSubtypes
			}
		}
	}

	private static IngredientRegistry registerIngredients(List<IModPlugin> plugins) {
		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();

		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				plugin.registerIngredients(modIngredientRegistry);
			} catch (RuntimeException e) {
				if (VanillaPlugin.class.isInstance(plugin)) {
					throw e;
				} else {
					Log.error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
					iterator.remove();
				}
			} catch (AbstractMethodError ignored) {
				if (VanillaPlugin.class.isInstance(plugin)) {
					throw ignored;
				}
				// legacy mod plugins do not have registerIngredients
			}
		}

		return modIngredientRegistry.createIngredientRegistry();
	}

	private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
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
	}

	private static void sendRuntime(List<IModPlugin> plugins, IJeiRuntime jeiRuntime) {
		Iterator<IModPlugin> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin plugin = iterator.next();
			try {
				long start_time = System.currentTimeMillis();
				Log.info("Sending runtime to plugin: {} ...", plugin.getClass().getName());
				plugin.onRuntimeAvailable(jeiRuntime);
				long timeElapsedMs = System.currentTimeMillis() - start_time;
				Log.info("Sent    runtime to plugin: {} in {} ms", plugin.getClass().getName(), timeElapsedMs);
			} catch (RuntimeException e) {
				Log.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			} catch (LinkageError e) {
				Log.error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
				iterator.remove();
			}
		}
	}
}
