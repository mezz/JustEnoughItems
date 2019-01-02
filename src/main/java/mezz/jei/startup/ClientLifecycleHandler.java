package mezz.jei.startup;

import java.io.File;
import java.util.List;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IReloadableResourceManager;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.HideModeConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.ModIdFormattingConfig;
import mezz.jei.config.ServerInfo;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.ingredients.ForgeModIdHelper;
import mezz.jei.network.PacketHandler;
import mezz.jei.network.PacketHandlerClient;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientLifecycleHandler {
	private final Logger LOGGER = LogManager.getLogger();
	private final JeiStarter starter = new JeiStarter();
	private final ClientConfig clientConfig;
	private final ModIdFormattingConfig modIdFormattingConfig;
	private final IModIdHelper modIdHelper;
	private final IHideModeConfig hideModeConfig;

	public ClientLifecycleHandler() {
		File jeiConfigurationDir = new File(FMLPaths.CONFIGDIR.get().toFile(), ModIds.JEI_ID);
		if (!jeiConfigurationDir.exists()) {
			try {
				if (!jeiConfigurationDir.mkdir()) {
					throw new RuntimeException("Could not create config directory " + jeiConfigurationDir);
				}
			} catch (SecurityException e) {
				throw new RuntimeException("Could not create config directory " + jeiConfigurationDir, e);
			}
		}
		clientConfig = new ClientConfig(jeiConfigurationDir);
		modIdFormattingConfig = new ModIdFormattingConfig(clientConfig.getConfig());
		modIdHelper = new ForgeModIdHelper(clientConfig, modIdFormattingConfig);
		ErrorUtil.setModIdHelper(modIdHelper);
		hideModeConfig = new HideModeConfig(modIdHelper, jeiConfigurationDir);

		KeyBindings.init();

//		eventBus.addListener(EventPriority.NORMAL, false, FMLLoadCompleteEvent.class, event -> this.onLoadComplete());
		EventBusHelper.addListener(GuiScreenEvent.KeyboardKeyPressedEvent.Pre.class, this::onGuiKeyPressedEvent);

		EventBusHelper.addListener(FMLPreInitializationEvent.class, event -> clientConfig.onPreInit());
		EventBusHelper.addListener(ConfigChangedEvent.OnConfigChangedEvent.class, event -> {
			modIdFormattingConfig.checkForModNameFormatOverride();
			clientConfig.onConfigChanged(event.getModID());
		});
		EventBusHelper.addListener(WorldEvent.Save.class, event -> clientConfig.onWorldSave());

		PacketHandlerClient packetHandler = new PacketHandlerClient();
		EventNetworkChannel channel = NetworkRegistry.newEventChannel(PacketHandler.CHANNEL_ID, () -> "1.0.0", s -> {
			boolean jeiOnServer = !NetworkRegistry.ABSENT.equals(s);
			ServerInfo.onConnectedToServer(jeiOnServer);
			return true;
		}, s -> true);
		channel.addListener(packetHandler::onPacket);
	}

	private void onRecipesLoaded() {
		List<IModPlugin> plugins = AnnotatedInstanceUtil.getModPlugins();

		// Reload when resources change
		Minecraft minecraft = Minecraft.getInstance();
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
		reloadableResourceManager.addReloadListener(resourceManager -> {
			// check that JEI has been started before. if not, do nothing
			if (this.starter.hasStarted()) {
				if (clientConfig.isDebugModeEnabled()) {
					LOGGER.info("Restarting JEI.", new RuntimeException("Stack trace for debugging"));
				} else {
					LOGGER.info("Restarting JEI.");
				}
				this.starter.start(plugins, clientConfig, hideModeConfig, modIdHelper);
			}
		});

		this.starter.start(plugins, clientConfig, hideModeConfig, modIdHelper);
	}

	/**
	 * temporary hack while waiting for recipe sync events in Forge
	 */
	private void onGuiKeyPressedEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		InputMappings.Input input = InputMappings.getInputByCode(event.getKeyCode(), event.getScanCode());
		if (KeyBindings.startJei.isActiveAndMatches(input)) {
			NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				NetworkManager networkManager = connection.getNetworkManager();
				clientConfig.syncWorldConfig(networkManager);
			}
			onRecipesLoaded();
			// TODO move this to its own event handler when the event exists
			modIdFormattingConfig.checkForModNameFormatOverride();
			EventBusHelper.post(new PlayerJoinedWorldEvent());
		}
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
