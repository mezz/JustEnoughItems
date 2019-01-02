package mezz.jei.load;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.fml.common.progress.ProgressBar;
import net.minecraftforge.fml.common.progress.StartupProgressManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.util.LoggedTimer;
import mezz.jei.util.StackHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginLoader {
	private static final Logger LOGGER = LogManager.getLogger();

	private final LoggedTimer timer;
	private final IModIdHelper modIdHelper;
	private final IngredientBlacklistInternal blacklist;
	private final ModRegistry modRegistry;
	private final IngredientRegistry ingredientRegistry;
	private final ClientConfig config;
	private final IHideModeConfig hideModeConfig;
	private final JeiHelpers jeiHelpers;
	@Nullable
	private RecipeRegistry recipeRegistry;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private BookmarkList bookmarkList;

	public PluginLoader(List<IModPlugin> plugins, ClientConfig config, IHideModeConfig hideModeConfig, IModIdHelper modIdHelper) {
		this.config = config;
		this.hideModeConfig = hideModeConfig;
		this.timer = new LoggedTimer();
		this.modIdHelper = modIdHelper;
		this.blacklist = new IngredientBlacklistInternal();

		sortPlugins(plugins);

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();
		registerItemSubtypes(plugins, subtypeRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		Internal.setStackHelper(stackHelper);

		ModIngredientRegistration modIngredientRegistry = registerIngredients(plugins);
		ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist, config.isDebugModeEnabled());
		Internal.setIngredientRegistry(ingredientRegistry);

		jeiHelpers = new JeiHelpers(ingredientRegistry, blacklist, stackHelper, hideModeConfig, modIdHelper);
		Internal.setHelpers(jeiHelpers);

		modRegistry = new ModRegistry(jeiHelpers, ingredientRegistry);

		timer.start("Registering recipe categories");
		registerCategories(plugins, modRegistry);
		timer.stop();

		timer.start("Registering mod plugins");
		registerPlugins(plugins, modRegistry);
		timer.stop();
	}

	public ModRegistry getModRegistry() {
		return modRegistry;
	}

	public JeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	public RecipeRegistry getRecipeRegistry() {
		if (recipeRegistry == null) {
			timer.start("Building recipe registry");
			recipeRegistry = modRegistry.createRecipeRegistry(ingredientRegistry);
			timer.stop();
		}
		return recipeRegistry;
	}

	public IngredientFilter getIngredientFilter() {
		if (ingredientFilter == null) {
			timer.start("Building ingredient list");
			NonNullList<IIngredientListElement> ingredientList = IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper);
			timer.stop();
			timer.start("Building ingredient filter");
			ingredientFilter = new IngredientFilter(blacklist, config, hideModeConfig);
			ingredientFilter.addIngredients(ingredientList);
			Internal.setIngredientFilter(ingredientFilter);
			timer.stop();
		}
		return ingredientFilter;
	}

	public IngredientRegistry getIngredientRegistry() {
		return ingredientRegistry;
	}

	public BookmarkList getBookmarkList() {
		if (bookmarkList == null) {
			timer.start("Building bookmarks");
			bookmarkList = new BookmarkList(ingredientRegistry, modIdHelper);
			bookmarkList.loadBookmarks();
			timer.stop();
		}
		return bookmarkList;
	}

	private static void sortPlugins(List<IModPlugin> plugins) {
		IModPlugin vanillaPlugin = getVanillaPlugin(plugins);
		if (vanillaPlugin != null) {
			plugins.remove(vanillaPlugin);
			plugins.add(0, vanillaPlugin);
		}

		IModPlugin jeiInternalPlugin = getJeiInternalPlugin(plugins);
		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	@Nullable
	private static IModPlugin getVanillaPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof VanillaPlugin) {
				return modPlugin;
			}
		}
		return null;
	}

	@Nullable
	private static IModPlugin getJeiInternalPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof JEIInternalPlugin) {
				return modPlugin;
			}
		}
		return null;
	}

	private static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
		try (ProgressBar progressBar = StartupProgressManager.start("Registering item subtypes", plugins.size())) {
			Iterator<IModPlugin> iterator = plugins.iterator();
			while (iterator.hasNext()) {
				IModPlugin plugin = iterator.next();
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					progressBar.step(pluginUid.toString());
					plugin.registerItemSubtypes(subtypeRegistry);
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
					iterator.remove();
				}
			}
		}
	}

	private static ModIngredientRegistration registerIngredients(List<IModPlugin> plugins) {
		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();
		try (ProgressBar progressBar = StartupProgressManager.start("Registering ingredients", plugins.size())) {
			Iterator<IModPlugin> iterator = plugins.iterator();
			while (iterator.hasNext()) {
				IModPlugin plugin = iterator.next();
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					progressBar.step(pluginUid.toString());
					plugin.registerIngredients(modIngredientRegistry);
				} catch (RuntimeException | LinkageError e) {
					if (plugin instanceof VanillaPlugin) {
						throw e;
					} else {
						LOGGER.error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
						iterator.remove();
					}
				}
			}
		}
		return modIngredientRegistry;
	}

	private static void registerCategories(List<IModPlugin> plugins, ModRegistry modRegistry) {
		try (ProgressBar progressBar = StartupProgressManager.start("Registering categories", plugins.size())) {
			Iterator<IModPlugin> iterator = plugins.iterator();
			while (iterator.hasNext()) {
				IModPlugin plugin = iterator.next();
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					progressBar.step(pluginUid.toString());
					long start_time = System.currentTimeMillis();
					LOGGER.debug("Registering categories: {} ...", pluginUid);
					plugin.registerCategories(modRegistry);
					long timeElapsedMs = System.currentTimeMillis() - start_time;
					LOGGER.debug("Registered  categories: {} in {} ms", pluginUid, timeElapsedMs);
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error("Failed to register mod categories: {}", plugin.getClass(), e);
					iterator.remove();
				}
			}
		}
	}

	private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
		try (ProgressBar progressBar = StartupProgressManager.start("Registering plugins", plugins.size())) {
			Iterator<IModPlugin> iterator = plugins.iterator();
			while (iterator.hasNext()) {
				IModPlugin plugin = iterator.next();
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					progressBar.step(pluginUid.toString());
					long start_time = System.currentTimeMillis();
					LOGGER.debug("Registering plugin: {} ...", pluginUid);
					plugin.register(modRegistry);
					long timeElapsedMs = System.currentTimeMillis() - start_time;
					LOGGER.debug("Registered  plugin: {} in {} ms", pluginUid, timeElapsedMs);
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error("Failed to register mod plugin: {}", plugin.getClass(), e);
					iterator.remove();
				}
			}
		}
	}

}
