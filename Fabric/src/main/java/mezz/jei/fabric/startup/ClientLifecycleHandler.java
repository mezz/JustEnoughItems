package mezz.jei.fabric.startup;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.network.ClientNetworkHandler;
import mezz.jei.fabric.network.ConnectionToServer;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private boolean running;

	public ClientLifecycleHandler() {
		IConnectionToServer serverConnection = new ConnectionToServer();
		Internal.setServerConnection(serverConnection);

		InternalKeyMappings keyMappings = new InternalKeyMappings(KeyMappingHelper::registerKeyMapping);
		Internal.setKeyMappings(keyMappings);

		ClientNetworkHandler.registerClientPacketHandler(serverConnection);

		List<IModPlugin> plugins = FabricPluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			serverConnection
		);

		this.jeiStarter = new JeiStarter(startData);
	}

	public void registerEvents() {
		JeiLifecycleEvents.GAME_START.register(() -> {
			JeiLifecycleEvents.AFTER_RECIPE_SYNC.register(() -> {
				if (running) {
					stopJei();
				}
				startJei();
			});
			ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
				if (!running) {
					if (screen instanceof AbstractContainerScreen && minecraft.player != null) {
						LOGGER.error("A Screen is opening but JEI hasn't started yet because the recipe sync event didn't happen.");
						startJei();
					}
				}
			});
		});
		JeiLifecycleEvents.GAME_STOP.register(this::stopJei);
	}

	public ResourceManagerReloadListener getReloadListener() {
		return (resourceManager) -> {
			if (running) {
				Minecraft minecraft = Minecraft.getInstance();
				if (!minecraft.isSameThread()) {
					// we may receive reload events on the server thread in single-player, ignore them
					return;
				}
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
