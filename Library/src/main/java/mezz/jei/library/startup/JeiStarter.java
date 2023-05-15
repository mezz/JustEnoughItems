package mezz.jei.library.startup;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.IAsyncModPlugin;
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
import mezz.jei.common.async.JeiStartTask;
import mezz.jei.common.config.ClientConfig;
import mezz.jei.common.config.ConfigManager;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.IClientToggleState;
import mezz.jei.common.config.JeiClientConfigs;
import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.FileWatcher;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.util.LoggedTimer;
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
import java.util.concurrent.CompletableFuture;

public final class JeiStarter {
	private static final Logger LOGGER = LogManager.getLogger();

	private final StartData data;
	private final VanillaPlugin vanillaPlugin;
	private final ModIdFormatConfig modIdFormatConfig;
	private final ColorNameConfig colorNameConfig;
	private final RecipeCategorySortingConfig recipeCategorySortingConfig;
	@SuppressWarnings("FieldCanBeLocal")
	private final FileWatcher fileWatcher = new FileWatcher("JEI Config File Watcher");
	private final ConfigManager configManager;
	private final JeiClientExecutor clientExecutor;
	private final PluginCaller pluginCaller;
	private final JeiClientConfigs jeiClientConfigs;

	private JeiStartTask currentStartTask = null;

	public JeiStarter(StartData data) {
		ErrorUtil.checkNotEmpty(data.plugins(), "plugins");
		this.data = data;
		List<IModPlugin> plugins = data.plugins();
		List<IAsyncModPlugin> asyncModPlugins = data.asyncPlugins();
		this.vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins, asyncModPlugins)
			.orElseThrow(() -> new IllegalStateException("vanilla plugin not found"));
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins, asyncModPlugins)
			.orElse(null);
		PluginHelper.sortPlugins(asyncModPlugins, vanillaPlugin, jeiInternalPlugin);

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

		this.jeiClientConfigs = new JeiClientConfigs(configDir.resolve("jei-client.ini"));
		this.jeiClientConfigs.register(fileWatcher, configManager);
		Internal.setJeiClientConfigs(jeiClientConfigs);

		fileWatcher.start();

		this.recipeCategorySortingConfig = new RecipeCategorySortingConfig(configDir.resolve("recipe-category-sort-order.ini"));

		this.clientExecutor = new JeiClientExecutor(new ClientTaskExecutor());
		this.pluginCaller = new PluginCaller(
			data.plugins(),
			data.asyncPlugins(),
			data.runtimePlugins(),
			clientExecutor,
			jeiClientConfigs.getClientConfig()
		);

		pluginCaller.callOnPlugins(
			"Sending ConfigManager",
			p -> p.onConfigManagerAvailable(configManager),
			p -> p.onConfigManagerAvailable(configManager, clientExecutor)
		);
	}

	/**
	 * Starts JEI, either synchronously or asynchronously depending on config. Should only be called from
	 * the main thread.
	 */
	public void start() {
		if (currentStartTask != null) {
			LOGGER.error("JEI start requested but it is already starting.");
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}

		if (jeiClientConfigs.getClientConfig().getAsyncLoadingEnabled()) {
			currentStartTask = new JeiStartTask(this::doActualStart);
			currentStartTask.start();
		} else {
			doActualStart();
		}
	}

	private void doActualStart() {
		LoggedTimer totalTime = new LoggedTimer();
		if (Thread.currentThread() instanceof JeiStartTask) {
			totalTime.start("Starting JEI asynchronously");
		} else {
			totalTime.start("Starting JEI synchronously");
		}

		IColorHelper colorHelper = new ColorHelper(colorNameConfig);

		IClientToggleState toggleState = Internal.getClientToggleState();

		PluginLoader pluginLoader = new PluginLoader(data.serverConnection(), pluginCaller, modIdFormatConfig, colorHelper, clientExecutor);
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
			vanillaPlugin,
			recipeCategorySortingConfig,
			modIdHelper,
			ingredientVisibility
		);
		ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers =
			pluginLoader.createRecipeTransferHandlers();
		IRecipeTransferManager recipeTransferManager = new RecipeTransferManager(recipeTransferHandlers);

		LoggedTimer timer = new LoggedTimer();
		timer.start("Building runtime");
		IScreenHelper screenHelper = pluginLoader.createGuiScreenHelper(jeiHelpers);

		RuntimeRegistration runtimeRegistration = new RuntimeRegistration(
			recipeManager,
			jeiHelpers,
			editModeConfig,
			ingredientManager,
			ingredientVisibility,
			recipeTransferManager,
			screenHelper
		);
		//noinspection removal
		pluginCaller.callOnPlugins(
			"Registering Runtime (legacy)",
			p -> p.registerRuntime(runtimeRegistration),
			p -> CompletableFuture.completedFuture(null)
		);
		pluginCaller.callOnRuntimePlugins(
			"Registering Runtime",
			p -> p.registerRuntime(runtimeRegistration, clientExecutor)
		);

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

		pluginCaller.callOnPlugins(
			"Sending Runtime",
			p -> p.onRuntimeAvailable(jeiRuntime),
			p -> p.onRuntimeAvailable(jeiRuntime, clientExecutor)
		);
		pluginCaller.callOnRuntimePlugins(
			"Registering Runtime",
			p -> p.onRuntimeAvailable(jeiRuntime, clientExecutor)
		);

		totalTime.stop();
	}

	public void stop() {
		LOGGER.info("Stopping JEI");
		if (currentStartTask != null) {
			currentStartTask.cancelStart();
			Minecraft.getInstance().managedBlock(() -> !currentStartTask.isAlive());
			currentStartTask = null;
		}
		pluginCaller.callOnPlugins(
			"Sending Runtime Unavailable",
			IModPlugin::onRuntimeUnavailable,
			p -> p.onRuntimeUnavailable(clientExecutor)
		);
	}

	public void tick() {
		this.clientExecutor.tick();
	}
}
