package mezz.jei.fabric;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.Services;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.fabric.config.ClientConfigs;
import mezz.jei.fabric.config.ServerConfig;
import mezz.jei.fabric.events.JeiIdentifiableResourceReloadListener;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.startup.ClientLifecycleHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

import java.nio.file.Path;

public class JustEnoughItemsClient implements ClientModInitializer {



	@Override
	public void onInitializeClient() {
		JeiLifecycleEvents.REGISTER_RESOURCE_RELOAD_LISTENER.register((resourceManager, textureManager) -> {
			JeiSpriteUploader spriteUploader = new JeiSpriteUploader(textureManager);
			resourceManager.registerReloadListener(new JeiIdentifiableResourceReloadListener("sprite_uploader", spriteUploader));

			Textures textures = new Textures(spriteUploader);
			Internal.setTextures(textures);

			ClientLifecycleEvents.CLIENT_STARTED.register(event -> {
				Path configDir = Services.PLATFORM.getConfigHelper().createJeiConfigDir();
				ClientConfigs jeiClientConfigs = new ClientConfigs(configDir.resolve("jei-client.ini"));

				IServerConfig serverConfig = ServerConfig.getInstance();
				ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler(textures, serverConfig, jeiClientConfigs);
				clientLifecycleHandler.registerEvents();

				ResourceManagerHelper.get(PackType.SERVER_DATA)
						.registerReloadListener(new JeiIdentifiableResourceReloadListener("lifecycle", clientLifecycleHandler.getReloadListener()));
			});
		});
	}
}
