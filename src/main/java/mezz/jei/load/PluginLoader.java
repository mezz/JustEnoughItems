package mezz.jei.load;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraftforge.fml.common.progress.ProgressBar;
import net.minecraftforge.fml.common.progress.StartupProgressManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Stopwatch;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.ICraftingRecipeWrapper;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientListElementFactory;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.ingredients.ModIngredientRegistration;
import mezz.jei.plugins.jei.JeiInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.util.ErrorUtil;
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
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IHideModeConfig hideModeConfig;
	private final JeiHelpers jeiHelpers;
	@Nullable
	private RecipeRegistry recipeRegistry;
	@Nullable
	private IngredientFilter ingredientFilter;
	@Nullable
	private BookmarkList bookmarkList;

	public PluginLoader(
		List<IModPlugin> plugins,
		Textures textures,
		IHideModeConfig hideModeConfig,
		IIngredientFilterConfig ingredientFilterConfig,
		IModIdHelper modIdHelper,
		boolean debugMode)
	{
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.hideModeConfig = hideModeConfig;
		this.timer = new LoggedTimer();
		this.modIdHelper = modIdHelper;
		this.blacklist = new IngredientBlacklistInternal();

		VanillaPlugin vanillaPlugin = getVanillaPlugin(plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
		JeiInternalPlugin jeiInternalPlugin = getJeiInternalPlugin(plugins);
		sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);

		SubtypeRegistry subtypeRegistry = new SubtypeRegistry();
		registerItemSubtypes(plugins, subtypeRegistry);

		ModIngredientRegistration modIngredientRegistry = registerIngredients(plugins);
		ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist, debugMode);
		Internal.setIngredientRegistry(ingredientRegistry);

		StackHelper stackHelper = new StackHelper(subtypeRegistry);
		GuiHelper guiHelper = new GuiHelper(ingredientRegistry, textures);
		jeiHelpers = new JeiHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper, hideModeConfig, modIdHelper);
		Internal.setHelpers(jeiHelpers);

		modRegistry = new ModRegistry(jeiHelpers, ingredientRegistry);

		timer.start("Registering recipe categories");
		registerCategories(plugins, modRegistry);
		timer.stop();

		timer.start("Registering vanilla category extensions");
		CraftingRecipeCategory craftingCategory = vanillaPlugin.getCraftingCategory();
		ErrorUtil.checkNotNull(craftingCategory, "vanilla crafting category");
		registerVanillaExtensions(plugins, craftingCategory);
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
			ingredientFilter = new IngredientFilter(blacklist, ingredientFilterConfig, hideModeConfig);
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

	private static void sortPlugins(List<IModPlugin> plugins, @Nullable IModPlugin vanillaPlugin, @Nullable IModPlugin jeiInternalPlugin) {
		if (vanillaPlugin != null) {
			plugins.remove(vanillaPlugin);
			plugins.add(0, vanillaPlugin);
		}

		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}
	}

	@Nullable
	private static VanillaPlugin getVanillaPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof VanillaPlugin) {
				return (VanillaPlugin) modPlugin;
			}
		}
		return null;
	}

	@Nullable
	private static JeiInternalPlugin getJeiInternalPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof JeiInternalPlugin) {
				return (JeiInternalPlugin) modPlugin;
			}
		}
		return null;
	}

	private static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
		callOnPlugins("Registering item subtypes", plugins, p -> p.registerItemSubtypes(subtypeRegistry));
	}

	private static ModIngredientRegistration registerIngredients(List<IModPlugin> plugins) {
		ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();
		callOnPlugins("Registering ingredients", plugins, p -> p.registerIngredients(modIngredientRegistry));
		return modIngredientRegistry;
	}

	private static void registerCategories(List<IModPlugin> plugins, ModRegistry modRegistry) {
		callOnPlugins("Registering categories", plugins, p -> p.registerCategories(modRegistry));
	}

	private static void registerVanillaExtensions(List<IModPlugin> plugins, IExtendableRecipeCategory<IRecipe, ICraftingRecipeWrapper> craftingCategory) {
		callOnPlugins("Registering vanilla category extensions", plugins, p -> p.registerVanillaCategoryExtensions(craftingCategory));
	}

	private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
		callOnPlugins("Registering plugins", plugins, p -> p.register(modRegistry));
	}

	private static void callOnPlugins(String title, List<IModPlugin> plugins, Consumer<IModPlugin> func) {
		List<IModPlugin> erroredPlugins = new ArrayList<>();
		try (ProgressBar progressBar = StartupProgressManager.start(title, plugins.size())) {
			for (IModPlugin plugin : plugins) {
				try {
					ResourceLocation pluginUid = plugin.getPluginUid();
					progressBar.step(pluginUid.toString());
					LOGGER.debug("{}: {} ...", title, pluginUid);
					Stopwatch stopwatch = Stopwatch.createStarted();
					func.accept(plugin);
					LOGGER.debug("{}: {} took {}", title, pluginUid, stopwatch);
				} catch (RuntimeException | LinkageError e) {
					if (plugin instanceof VanillaPlugin) {
						throw e;
					}
					LOGGER.error("Caught an error from mod plugin: {} {}", plugin.getClass(), plugin.getPluginUid(), e);
					erroredPlugins.add(plugin);
				}
			}
		}
		plugins.removeAll(erroredPlugins);
	}

}
