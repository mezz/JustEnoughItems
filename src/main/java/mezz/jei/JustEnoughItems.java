package mezz.jei;

import mezz.jei.api.constants.ModIds;
import mezz.jei.config.JEIClientConfig;
import mezz.jei.events.EventBusHelper;
import mezz.jei.gui.textures.JeiSpriteUploader;
import mezz.jei.gui.textures.Textures;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModIds.JEI_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		NetworkHandler networkHandler = new NetworkHandler();
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientStart(modEventBus, networkHandler));
		commonStart(modEventBus, networkHandler);
	}

	private static void clientStart(IEventBus modEventBus, NetworkHandler networkHandler) {
		JEIClientConfig.register();

		EventBusHelper.addListener(JustEnoughItems.class, modEventBus, ColorHandlerEvent.Block.class, setupEvent -> {
			Minecraft minecraft = Minecraft.getInstance();
			JeiSpriteUploader spriteUploader = new JeiSpriteUploader(minecraft.textureManager);
			Textures textures = new Textures(spriteUploader);
			IResourceManager resourceManager = minecraft.getResourceManager();
			if (resourceManager instanceof IReloadableResourceManager) {
				IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) resourceManager;
				reloadableResourceManager.registerReloadListener(spriteUploader);
			}
			EventBusHelper.addLifecycleListener(JustEnoughItems.class, modEventBus, FMLLoadCompleteEvent.class, loadCompleteEvent ->
				new ClientLifecycleHandler(networkHandler, textures)
			);
		});
	}

	private static void commonStart(IEventBus modEventBus, NetworkHandler networkHandler) {
		EventBusHelper.addLifecycleListener(JustEnoughItems.class, modEventBus, FMLCommonSetupEvent.class, event ->
			networkHandler.createServerPacketHandler()
		);
	}
}
