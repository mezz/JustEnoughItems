package mezz.jei.forge;

import mezz.jei.common.Internal;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.forge.startup.ClientLifecycleHandler;
import mezz.jei.forge.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

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
		// Add the Sprite uploader reload listener
		Minecraft minecraft = Minecraft.getInstance();
		JeiSpriteUploader spriteUploader = new JeiSpriteUploader(minecraft.textureManager);
		Textures textures = new Textures(spriteUploader);
		Internal.setTextures(textures);
		event.registerReloadListener(spriteUploader);

		ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler(networkHandler, textures, serverConfig);
		clientLifecycleHandler.register(subscriptions);
		event.registerReloadListener(clientLifecycleHandler.getReloadListener());
	}
}
