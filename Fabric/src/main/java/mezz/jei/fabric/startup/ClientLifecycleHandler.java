package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.network.ClientNetworkHandler;
import mezz.jei.fabric.network.ConnectionToServer;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private boolean running;

	public ClientLifecycleHandler(Textures textures, IServerConfig serverConfig) {
		IConnectionToServer serverConnection = new ConnectionToServer();
		Internal.setServerConnection(serverConnection);

		InternalKeyMappings keyMappings = new InternalKeyMappings(keyMapping -> {});
		Internal.setKeyMappings(keyMappings);

		IWorldConfig worldConfig = Internal.getWorldConfig();
		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig, worldConfig);
		ClientNetworkHandler.registerClientPacketHandler(packetRouter);

		List<IModPlugin> plugins = FabricPluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			textures,
			serverConnection,
			keyMappings
		);

		this.jeiStarter = new JeiStarter(startData);
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

		this.jeiStarter.start();
		running = true;
	}

	private void stopJei() {
		LOGGER.info("Stopping JEI");
		this.jeiStarter.stop();
		running = false;
	}
}
