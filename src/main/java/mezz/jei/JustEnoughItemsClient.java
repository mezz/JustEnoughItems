package mezz.jei;

import mezz.jei.config.JEIClientConfigs;
import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.gui.textures.JeiSpriteUploader;
import mezz.jei.gui.textures.Textures;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public class JustEnoughItemsClient {
	private final NetworkHandler networkHandler;
	private final PermanentEventSubscriptions subscriptions;
	private final JEIClientConfigs jeiClientConfigs = new JEIClientConfigs();

	public JustEnoughItemsClient(NetworkHandler networkHandler, PermanentEventSubscriptions subscriptions) {
		this.networkHandler = networkHandler;
		this.subscriptions = subscriptions;
	}

	public void register() {
		jeiClientConfigs.register(subscriptions);
		subscriptions.register(RegisterClientReloadListenersEvent.class, event -> this.onRegisterReloadListenerEvent(event, subscriptions));
	}

	private void onRegisterReloadListenerEvent(RegisterClientReloadListenersEvent event, PermanentEventSubscriptions subscriptions) {
		// Add the Sprite uploader reload listener
		Minecraft minecraft = Minecraft.getInstance();
		JeiSpriteUploader spriteUploader = new JeiSpriteUploader(minecraft.textureManager);
		Textures textures = new Textures(spriteUploader);
		Internal.setTextures(textures);
		event.registerReloadListener(spriteUploader);

		ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler(networkHandler, textures, jeiClientConfigs);
		clientLifecycleHandler.register(subscriptions);
		event.registerReloadListener(clientLifecycleHandler);
	}
}
