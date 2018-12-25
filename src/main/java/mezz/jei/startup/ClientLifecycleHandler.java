package mezz.jei.startup;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;

import mezz.jei.api.IModPlugin;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.ServerInfo;
import mezz.jei.network.PacketHandler;
import mezz.jei.network.PacketHandlerClient;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.util.Log;

public class ClientLifecycleHandler {
	private final JeiStarter starter = new JeiStarter();

	public ClientLifecycleHandler() {
		KeyBindings.init();

		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(EventPriority.NORMAL, false, FMLPreInitializationEvent.class, event -> this.onPreInit());
		eventBus.addListener(EventPriority.NORMAL, false, FMLLoadCompleteEvent.class, event -> this.onLoadComplete());
//		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, FMLLo

		File jeiConfigurationDir = new File(FMLPaths.CONFIGDIR.get().toFile(), Constants.MOD_ID);
		ClientConfig clientConfig = new ClientConfig(eventBus, jeiConfigurationDir);
		eventBus.addListener(EventPriority.NORMAL, false, FMLPreInitializationEvent.class, event -> clientConfig.onPreInit());
		eventBus.addListener(EventPriority.NORMAL, false, ConfigChangedEvent.OnConfigChangedEvent.class, event -> clientConfig.onConfigChanged(event.getModID()));
		eventBus.addListener(EventPriority.NORMAL, false, WorldEvent.Save.class, event -> clientConfig.onWorldSave());
	}

	private void onPreInit() {
		PacketHandlerClient packetHandler = new PacketHandlerClient();
		EventNetworkChannel channel = NetworkRegistry.newEventChannel(PacketHandler.CHANNEL_ID, () -> "1.0.0", s -> {
			boolean jeiOnServer = !NetworkRegistry.ABSENT.equals(s);
			ServerInfo.onConnectedToServer(jeiOnServer);
			return true;
		}, s -> true);
		channel.addListener(packetHandler::onPacket);
	}

	@Nullable
	private static IModPlugin getVanillaPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof VanillaPlugin) {
				return modPlugin;
			}
		}
		return null;
	}

	@Nullable
	private static IModPlugin getJeiInternalPlugin(List<IModPlugin> modPlugins) {
		for (IModPlugin modPlugin : modPlugins) {
			if (modPlugin instanceof JEIInternalPlugin) {
				return modPlugin;
			}
		}
		return null;
	}

	private void onLoadComplete() {
		List<IModPlugin> plugins = AnnotatedInstanceUtil.getModPlugins();

		IModPlugin vanillaPlugin = getVanillaPlugin(plugins);
		if (vanillaPlugin != null) {
			plugins.remove(vanillaPlugin);
			plugins.add(0, vanillaPlugin);
		}

		IModPlugin jeiInternalPlugin = getJeiInternalPlugin(plugins);
		if (jeiInternalPlugin != null) {
			plugins.remove(jeiInternalPlugin);
			plugins.add(jeiInternalPlugin);
		}

		// Reload when resources change
		Minecraft minecraft = Minecraft.getInstance();
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
		reloadableResourceManager.addReloadListener(resourceManager -> {
			// check that JEI has been started before. if not, do nothing
			if (this.starter.hasStarted()) {
				if (ClientConfig.getInstance().isDebugModeEnabled()) {
					Log.get().info("Restarting JEI.", new RuntimeException("Stack trace for debugging"));
				} else {
					Log.get().info("Restarting JEI.");
				}
				this.starter.start(plugins);
			}
		});

		this.starter.start(plugins);
	}

	// TODO
//	@SubscribeEvent
//	public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
//		if (!event.isLocal() && !event.getConnectionType().equals("MODDED")) {
//			ServerInfo.onConnectedToServer(false);
//		}
//		NetworkManager networkManager = event.getManager();
//		ClientConfig.getInstance().syncWorldConfig(networkManager);
//		MinecraftForge.EVENT_BUS.post(new PlayerJoinedWorldEvent());
//	}

}
