package mezz.jei.fabric;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.MinecraftLocaleSupplier;
import mezz.jei.common.util.Translator;
import mezz.jei.fabric.events.JeiIdentifiableResourceReloadListener;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.plugins.fabric.FabricGuiPlugin;
import mezz.jei.fabric.startup.ClientLifecycleHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@SuppressWarnings("unused")
public class JustEnoughItemsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Translator.setLocaleSupplier(new MinecraftLocaleSupplier());
		ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler();

		JeiLifecycleEvents.REGISTER_RESOURCE_RELOAD_LISTENER.register((resourceManager, textureManager) -> {
			Textures textures = Internal.getTextures();
			JeiSpriteUploader spriteUploader = textures.getSpriteUploader();
			resourceManager.registerReloadListener(new JeiIdentifiableResourceReloadListener("sprite_uploader", spriteUploader));

			ClientLifecycleEvents.CLIENT_STARTED.register(event -> {
				clientLifecycleHandler.registerEvents();

				ResourceManagerHelper.get(PackType.SERVER_DATA)
						.registerReloadListener(new JeiIdentifiableResourceReloadListener("lifecycle", clientLifecycleHandler.getReloadListener()));

				ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
					.registerReloadListener(new JeiIdentifiableResourceReloadListener("resources_reload", createReloadListener()));
			});
		});
	}

	public ResourceManagerReloadListener createReloadListener() {
		return (resourceManager) -> {
			FabricGuiPlugin.getResourceReloadHandler()
				.ifPresent(r -> r.onResourceManagerReload(resourceManager));
		};
	}
}
