package mezz.jei.fabric.startup;

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
import mezz.jei.fabric.config.KeyBindings;
import mezz.jei.fabric.network.ClientNetworkHandler;
import mezz.jei.fabric.network.ConnectionToServer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
		KeyBindings keyBindings = new KeyBindings();
		keyBindings.register();

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
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> startJei());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> stopJei());
	}

	public PreparableReloadListener getReloadListener() {
		return (preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2) -> {
			if (running) {
				stopJei();
				startJei();
			}
			return CompletableFuture.completedFuture(null);
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
