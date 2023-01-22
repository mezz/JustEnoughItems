package mezz.jei.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.config.IWorldConfig;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.network.ConnectionToServer;
import mezz.jei.forge.network.NetworkHandler;
import mezz.jei.forge.startup.ForgePluginFinder;
import mezz.jei.forge.startup.StartEventObserver;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JustEnoughItemsClient {
	private final NetworkHandler networkHandler;
	private final PermanentEventSubscriptions subscriptions;
	private final IServerConfig serverConfig;

	public JustEnoughItemsClient(NetworkHandler networkHandler, PermanentEventSubscriptions subscriptions, IServerConfig serverConfig) {
		this.networkHandler = networkHandler;
		this.subscriptions = subscriptions;
		this.serverConfig = serverConfig;
	}

	public void register() {
		subscriptions.register(RegisterClientReloadListenersEvent.class, this::onRegisterReloadListenerEvent);
	}

	private void onRegisterReloadListenerEvent(RegisterClientReloadListenersEvent event) {
		Textures textures = createTextures(event);
		Internal.setTextures(textures);

		ConnectionToServer serverConnection = new ConnectionToServer(networkHandler);
		Internal.setServerConnection(serverConnection);

		IWorldConfig worldConfig = Internal.getWorldConfig();

		ClientPacketRouter packetRouter = new ClientPacketRouter(serverConnection, serverConfig, worldConfig);
		networkHandler.registerClientPacketHandler(packetRouter);

		InternalKeyMappings keyMappings = createKeyMappings(subscriptions);
		Internal.setKeyMappings(keyMappings);

		List<IModPlugin> plugins = ForgePluginFinder.getModPlugins();
		StartData startData = new StartData(
			plugins,
			textures,
			serverConnection,
			keyMappings
		);

		JeiStarter jeiStarter = new JeiStarter(startData);
		StartEventObserver startEventObserver = new StartEventObserver(jeiStarter::start, jeiStarter::stop);
		startEventObserver.register(subscriptions);
		event.registerReloadListener(startEventObserver);
	}

	private static Textures createTextures(RegisterClientReloadListenersEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.textureManager;
		JeiSpriteUploader spriteUploader = new JeiSpriteUploader(textureManager);
		event.registerReloadListener(spriteUploader);
		return new Textures(spriteUploader);
	}

	private static InternalKeyMappings createKeyMappings(PermanentEventSubscriptions subscriptions) {
		Set<KeyMapping> keysToRegister = new HashSet<>();
		subscriptions.register(RegisterKeyMappingsEvent.class, e -> keysToRegister.forEach(e::register));
		return new InternalKeyMappings(keysToRegister::add);
	}
}
