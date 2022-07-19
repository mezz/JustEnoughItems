package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.config.InternalKeyMappings;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.startup.ConfigData;
import mezz.jei.common.startup.JeiEventHandlers;
import mezz.jei.common.startup.JeiStarter;
import mezz.jei.common.startup.StartData;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.network.ClientNetworkHandler;
import mezz.jei.fabric.network.ConnectionToServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private final EventRegistration eventRegistration;
	private boolean running;

	public ClientLifecycleHandler(Textures textures, IServerConfig serverConfig) {
		FabricLoader fabricLoader = FabricLoader.getInstance();
		Path configDir = fabricLoader.getConfigDir();
		Path jeiConfigDir = configDir.resolve(ModIds.JEI_ID);

		IConnectionToServer serverConnection = new ConnectionToServer();
		InternalKeyMappings keyBindings = new InternalKeyMappings(keyMapping -> {});

		ConfigData configData = ConfigData.create(
			serverConnection,
			keyBindings,
			jeiConfigDir
		);

		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig, configData.worldConfig());
		ClientNetworkHandler.registerClientPacketHandler(packetRouter);

		List<IModPlugin> plugins = FabricPluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			textures,
			serverConnection,
			keyBindings,
			configData
		);

		this.jeiStarter = new JeiStarter(startData);
		this.eventRegistration = new EventRegistration();
	}

	public void registerEvents() {
		JeiLifecycleEvents.GAME_START.register(() ->
			JeiLifecycleEvents.AFTER_RECIPE_SYNC.register(() -> {
				if (running) {
					stopJei();
				}
				startJei();
			})
		);
		JeiLifecycleEvents.GAME_STOP.register(this::stopJei);
	}

	public ResourceManagerReloadListener getReloadListener() {
		return (resourceManager) -> {
			if (running) {
				stopJei();
				startJei();
			}
		};
	}

	private void startJei() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}
		if (running) {
			LOGGER.error("Failed to start JEI, it is already running.");
			return;
		}

		JeiEventHandlers handlers = this.jeiStarter.start();
		eventRegistration.setEventHandlers(handlers);
		running = true;
	}

	private void stopJei() {
		LOGGER.info("Stopping JEI");
		this.eventRegistration.clear();
		Internal.setRuntime(null);
		running = false;
	}
}
