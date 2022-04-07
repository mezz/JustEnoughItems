package mezz.jei.forge.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.config.BookmarkConfig;
import mezz.jei.common.config.EditModeConfig;
import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.common.config.sorting.ModNameSortingConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.ingredients.IngredientSorter;
import mezz.jei.common.load.PluginHelper;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.plugins.jei.JeiInternalPlugin;
import mezz.jei.common.plugins.vanilla.VanillaPlugin;
import mezz.jei.common.startup.ConfigData;
import mezz.jei.common.startup.JeiEventHandlers;
import mezz.jei.common.startup.JeiStarter;
import mezz.jei.common.startup.StartData;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.forge.config.ForgeKeyBindings;
import mezz.jei.forge.config.JEIClientConfigs;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.network.ConnectionToServer;
import mezz.jei.forge.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private final StartEventObserver startEventObserver = new StartEventObserver(this::startJei, this::stopJei);
	private final RuntimeEventSubscriptions runtimeSubscriptions;

	public ClientLifecycleHandler(NetworkHandler networkHandler, Textures textures, IServerConfig serverConfig) {
		File jeiConfigurationDir = createConfigDir();
		JEIClientConfigs jeiClientConfigs = new JEIClientConfigs();
		jeiClientConfigs.register();

		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();

		// Additional config files
		IBookmarkConfig bookmarkConfig = new BookmarkConfig(jeiConfigurationDir);
		IEditModeConfig editModeConfig = new EditModeConfig(jeiConfigurationDir);
		RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(new File(jeiConfigurationDir, "recipe-category-sort-order.ini"));

		IIngredientSorter ingredientSorter = createIngredientSorter(clientConfig, jeiConfigurationDir);

		IConnectionToServer serverConnection = new ConnectionToServer();
		Internal.setServerConnection(serverConnection);

		ForgeKeyBindings keyBindings = new ForgeKeyBindings();
		keyBindings.register();

		WorldConfig worldConfig = new WorldConfig(serverConnection, keyBindings);
		networkHandler.createClientPacketHandler(serverConnection, serverConfig, worldConfig);

		List<IModPlugin> plugins = ForgePluginFinder.getModPlugins();
		VanillaPlugin vanillaPlugin = PluginHelper.getPluginWithClass(VanillaPlugin.class, plugins);
		JeiInternalPlugin jeiInternalPlugin = PluginHelper.getPluginWithClass(JeiInternalPlugin.class, plugins);
		ErrorUtil.checkNotNull(vanillaPlugin, "vanilla plugin");
		PluginHelper.sortPlugins(plugins, vanillaPlugin, jeiInternalPlugin);

		ConfigData configData = new ConfigData(
			clientConfig,
			editModeConfig,
			jeiClientConfigs.getFilterConfig(),
			worldConfig,
			bookmarkConfig,
			jeiClientConfigs.getIngredientListConfig(),
			jeiClientConfigs.getBookmarkListConfig(),
			recipeCategorySortingConfig,
			jeiClientConfigs.getModNameFormat()
		);

		StartData startData = new StartData(
			plugins,
			vanillaPlugin,
			textures,
			serverConnection,
			ingredientSorter,
			keyBindings,
			configData
		);

		this.jeiStarter = new JeiStarter(startData);
		this.runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		this.startEventObserver.register(subscriptions);
	}

	public PreparableReloadListener getReloadListener() {
		return this.startEventObserver;
	}

	private void startJei() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}
		if (!this.runtimeSubscriptions.isEmpty()) {
			LOGGER.error("Failed to start JEI, it is already running.");
			return;
		}

		JeiEventHandlers handlers = this.jeiStarter.start();
		EventRegistration.registerEvents(this.runtimeSubscriptions, handlers);
	}

	private void stopJei() {
		LOGGER.info("Stopping JEI");
		this.runtimeSubscriptions.clear();
		Internal.setRuntime(null);
	}

	private static IIngredientSorter createIngredientSorter(IClientConfig clientConfig, File jeiConfigurationDir) {
		ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(new File(jeiConfigurationDir, "ingredient-list-mod-sort-order.ini"));
		IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(new File(jeiConfigurationDir, "ingredient-list-type-sort-order.ini"));
		return new IngredientSorter(clientConfig, ingredientModNameSortingConfig, ingredientTypeSortingConfig);
	}

	private static File createConfigDir() {
		File configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), ModIds.JEI_ID);
		if (!configDir.exists()) {
			try {
				if (!configDir.mkdir()) {
					throw new RuntimeException("Could not create config directory " + configDir);
				}
			} catch (SecurityException e) {
				throw new RuntimeException("Could not create config directory " + configDir, e);
			}
		}
		return configDir;
	}
}
