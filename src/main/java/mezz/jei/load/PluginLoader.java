package mezz.jei.load;

import java.util.Iterator;
import java.util.List;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.AbstractModIdHelper;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.ModInfoRegistration;
import mezz.jei.startup.ModRegistry;
import mezz.jei.startup.StartupProgress;
import mezz.jei.util.Log;

/** Runs plugin registration phases independently of the client GUI lifecycle. */
public final class PluginLoader {
	private PluginLoader() {
	}

	public static void registerModInfo(List<IModPlugin> plugins, IModIdHelper modIdHelper) {
		if (!(modIdHelper instanceof AbstractModIdHelper)) {
			return;
		}

		ModInfoRegistration registration = new ModInfoRegistration();
		try (StartupProgress progressBar = StartupProgress.start("Registering mod info", plugins.size())) {
			for (IModPlugin plugin : plugins) {
				try {
					progressBar.step(plugin.getClass().getName());
					plugin.registerModInfo(registration);
				} catch (RuntimeException | LinkageError e) {
					if (plugin instanceof VanillaPlugin) {
						throw e;
					}
					Log.get().error("Failed to register mod info for mod plugin: {}", plugin.getClass(), e);
				}
			}
		}
		((AbstractModIdHelper) modIdHelper).setModAliases(registration.getModAliases());
	}

	public static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
		try (StartupProgress progressBar = StartupProgress.start("Registering item subtypes", plugins.size())) {
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
		}
	}

	public static ModIngredientRegistration registerIngredients(List<IModPlugin> plugins) {
		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();
		try (StartupProgress progressBar = StartupProgress.start("Registering ingredients", plugins.size())) {

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
		}

		return modIngredientRegistry;
	}

	public static void registerIngredientAliases(List<IModPlugin> plugins, ModIngredientRegistration modIngredientRegistry) {
		try (StartupProgress progressBar = StartupProgress.start("Registering ingredient aliases", plugins.size())) {
			for (IModPlugin plugin : plugins) {
				try {
					progressBar.step(plugin.getClass().getName());
					plugin.registerIngredientAliases(modIngredientRegistry);
				} catch (RuntimeException | LinkageError e) {
					if (plugin instanceof VanillaPlugin) {
						throw e;
					}
					Log.get().error("Failed to register ingredient aliases for mod plugin: {}", plugin.getClass(), e);
				}
			}
		}
	}

	public static void registerCategories(List<IModPlugin> plugins, ModRegistry modRegistry) {
		try (StartupProgress progressBar = StartupProgress.start("Registering categories", plugins.size())) {
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
		}
	}

	public static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
		try (StartupProgress progressBar = StartupProgress.start("Registering plugins", plugins.size())) {
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
		}
	}

	public static void sendRuntime(List<IModPlugin> plugins, IJeiRuntime jeiRuntime) {
		try (StartupProgress progressBar = StartupProgress.start("Sending Runtime", plugins.size())) {
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
		}
	}

}
