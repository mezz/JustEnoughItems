package mezz.jei.fabric;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.JeiSpriteUploader;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.fabric.config.ServerConfig;
import mezz.jei.fabric.events.JeiIdentifiableResourceReloadListener;
import mezz.jei.fabric.mixin.MinecraftAccess;
import mezz.jei.fabric.startup.ClientLifecycleHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.PackType;

public class JustEnoughItemsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		IServerConfig serverConfig = ServerConfig.getInstance();
		ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler(serverConfig);
		clientLifecycleHandler.registerEvents();

		ResourceManagerHelper.get(PackType.SERVER_DATA)
			.registerReloadListener(new JeiIdentifiableResourceReloadListener("lifecycle", clientLifecycleHandler.getReloadListener()));
	}
}
