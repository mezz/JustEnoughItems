package mezz.jei.forge.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.startup.ConfigData;
import mezz.jei.common.startup.JeiEventHandlers;
import mezz.jei.common.startup.JeiStarter;
import mezz.jei.common.startup.StartData;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
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

		IConnectionToServer serverConnection = new ConnectionToServer();
		Internal.setServerConnection(serverConnection);

		ForgeKeyBindings keyBindings = new ForgeKeyBindings();
		keyBindings.register();

		ConfigData configData = ConfigData.create(
			jeiClientConfigs.getClientConfig(),
			jeiClientConfigs.getFilterConfig(),
			jeiClientConfigs.getIngredientListConfig(),
			jeiClientConfigs.getBookmarkListConfig(),
			jeiClientConfigs.getModNameFormat(),
			serverConnection,
			keyBindings,
			jeiConfigurationDir
		);

		IWorldConfig worldConfig = configData.worldConfig();
		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig, worldConfig);
		networkHandler.createClientPacketHandler(packetRouter);

		List<IModPlugin> plugins = ForgePluginFinder.getModPlugins();

		StartData startData = new StartData(
			plugins,
			textures,
			serverConnection,
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
