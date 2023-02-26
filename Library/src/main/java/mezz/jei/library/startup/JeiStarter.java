package mezz.jei.library.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.Internal;
import mezz.jei.common.config.ConfigManager;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.JeiClientConfigs;
import mezz.jei.common.config.file.FileWatcher;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.util.LoggedTimer;
import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.library.color.ColorHelper;
import mezz.jei.library.config.ColorNameConfig;
import mezz.jei.library.config.EditModeConfig;
import mezz.jei.library.config.ModIdFormatConfig;
import mezz.jei.library.config.RecipeCategorySortingConfig;
import mezz.jei.library.ingredients.IngredientBlacklistInternal;
import mezz.jei.library.ingredients.IngredientVisibility;
import mezz.jei.library.load.PluginCaller;
import mezz.jei.library.load.PluginHelper;
import mezz.jei.library.load.PluginLoader;
import mezz.jei.library.load.registration.RuntimeRegistration;
import mezz.jei.library.plugins.jei.JeiInternalPlugin;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.recipes.RecipeManager;
import mezz.jei.library.recipes.RecipeTransferManager;
import mezz.jei.library.runtime.JeiHelpers;
import mezz.jei.library.runtime.JeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public final class JeiStarter {
	private static final Logger LOGGER = LogManager.getLogger();

	private final StartData data;
	private final List<IModPlugin> plugins;
	private final VanillaPlugin vanillaPlugin;
	private final ModIdFormatConfig modIdFormatConfig;
	private final ColorNameConfig colorNameConfig;
	private final RecipeCategorySortingConfig recipeCategorySortingConfig;
	@SuppressWarnings("FieldCanBeLocal")
	private final FileWatcher fileWatcher = new FileWatcher("JEI Config File Watcher");
	private final ConfigManager configManager;

	public JeiStarter(StartData data) {
		ErrorUtil.checkNotEmpty(data.plugins(), "plugins");
		this.data = data;
		this.plugins = data.plugins();
		this.vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins)
			.orElseThrow(() -> new IllegalStateException("vanilla plugin not found"));
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins)
			.orElse(null);
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);

		Path configDir = Services.PLATFORM.getConfigHelper().createJeiConfigDir();

		this.configManager = new ConfigManager();

		IConfigSchemaBuilder debugFileBuilder = new ConfigSchemaBuilder(configDir.resolve("jei-debug.ini"));
		DebugConfig.create(debugFileBuilder);
		debugFileBuilder.build().register(fileWatcher, configManager);

		IConfigSchemaBuilder modFileBuilder = new ConfigSchemaBuilder(configDir.resolve("jei-mod-id-format.ini"));
		this.modIdFormatConfig = new ModIdFormatConfig(modFileBuilder);
		modFileBuilder.build().register(fileWatcher, configManager);

		IConfigSchemaBuilder colorFileBuilder = new ConfigSchemaBuilder(configDir.resolve("jei-colors.ini"));
		this.colorNameConfig = new ColorNameConfig(colorFileBuilder);
		colorFileBuilder.build().register(fileWatcher, configManager);

		JeiClientConfigs jeiClientConfigs = new JeiClientConfigs(configDir.resolve("jei-client.ini"));
		jeiClientConfigs.register(fileWatcher, configManager);
		Internal.setJeiClientConfigs(jeiClientConfigs);

		fileWatcher.start();

		this.recipeCategorySortingConfig = new RecipeCategorySortingConfig(configDir.resolve("recipe-category-sort-order.ini"));

		PluginCaller.callOnPlugins("Sending ConfigManager", plugins, p -> p.onConfigManagerAvailable(configManager));
	}

	public void start() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}

		LoggedTimer totalTime = new LoggedTimer();
		totalTime.start("Starting JEI");

		IColorHelper colorHelper = new ColorHelper(colorNameConfig);

		IClientToggleState toggleState = Internal.getClientToggleState();

		PluginLoader pluginLoader = new PluginLoader(data, modIdFormatConfig, colorHelper);
		JeiHelpers jeiHelpers = pluginLoader.getJeiHelpers();
		IModIdHelper modIdHelper = jeiHelpers.getModIdHelper();

		IIngredientManager ingredientManager = pluginLoader.getIngredientManager();

		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		ingredientManager.registerIngredientListener(blacklist);

		Path configDir = Services.PLATFORM.getConfigHelper().createJeiConfigDir();
		EditModeConfig editModeConfig = new EditModeConfig(new EditModeConfig.FileSerializer(configDir.resolve("blacklist.cfg")), ingredientManager);

		IIngredientVisibility ingredientVisibility = new IngredientVisibility(
			blacklist,
			toggleState,
			editModeConfig,
			ingredientManager
		);

		RecipeManager recipeManager = pluginLoader.createRecipeManager(
			plugins,
			vanillaPlugin,
			recipeCategorySortingConfig,
			modIdHelper,
			ingredientVisibility
		);
		ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers =
			pluginLoader.createRecipeTransferHandlers(plugins);
		IRecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		IScreenHelper screenHelper = pluginLoader.createGuiScreenHelper(plugins, jeiHelpers);

		RuntimeRegistration runtimeRegistration = new RuntimeRegistration(
			recipeManager,
			jeiHelpers,
			editModeConfig,
			ingredientManager,
			ingredientVisibility,
			recipeTransferManager,
			screenHelper
		);
		PluginCaller.callOnPlugins("Registering Runtime", plugins, p -> p.registerRuntime(runtimeRegistration));

		JeiRuntime jeiRuntime = new JeiRuntime(
			recipeManager,
			ingredientManager,
			ingredientVisibility,
			data.keyBindings(),
			jeiHelpers,
			screenHelper,
			recipeTransferManager,
			editModeConfig,
			runtimeRegistration.getIngredientListOverlay(),
			runtimeRegistration.getBookmarkOverlay(),
			runtimeRegistration.getRecipesGui(),
			runtimeRegistration.getIngredientFilter(),
			configManager
		);
		timer.stop();

		PluginCaller.callOnPlugins("Sending Runtime", plugins, p -> p.onRuntimeAvailable(jeiRuntime));

		totalTime.stop();
	}

	public void stop() {
		LOGGER.info("Stopping JEI");
		List<IModPlugin> plugins = data.plugins();
		PluginCaller.callOnPlugins("Sending Runtime Unavailable", plugins, IModPlugin::onRuntimeUnavailable);
	}
}
