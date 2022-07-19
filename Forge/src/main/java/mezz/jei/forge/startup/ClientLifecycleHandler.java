package mezz.jei.forge.startup;

import java.util.HashSet;
import java.util.Set;
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
import mezz.jei.common.config.InternalKeyMappings;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.network.ConnectionToServer;
import mezz.jei.forge.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private final StartEventObserver startEventObserver = new StartEventObserver(this::startJei, this::stopJei);
	private final RuntimeEventSubscriptions runtimeSubscriptions;
	private final Set<KeyMapping> keysToRegister = new HashSet<>();

	public ClientLifecycleHandler(NetworkHandler networkHandler, Textures textures, IServerConfig serverConfig) {
		Path configDir = FMLPaths.CONFIGDIR.get();
		Path jeiConfigDir = configDir.resolve(ModIds.JEI_ID);

		IConnectionToServer serverConnection = new ConnectionToServer();
		InternalKeyMappings keyBindings = new InternalKeyMappings(keysToRegister::add);

		ConfigData configData = ConfigData.create(
			serverConnection,
			keyBindings,
			jeiConfigDir
		);

		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig, configData.worldConfig());
		networkHandler.registerClientPacketHandler(packetRouter);

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
		subscriptions.register(RegisterKeyMappingsEvent.class, event -> keysToRegister.forEach(event::register));
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
}
