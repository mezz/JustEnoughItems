package mezz.jei.startup;

import java.io.File;
import java.util.List;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IReloadableResourceManager;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.config.ClientConfig;
import mezz.jei.config.HideModeConfig;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.IngredientFilterConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.ModIdFormattingConfig;
import mezz.jei.config.WorldConfig;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.ingredients.ForgeModIdHelper;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientLifecycleHandler {
	private final Logger LOGGER = LogManager.getLogger();
	private final JeiStarter starter = new JeiStarter();
	private final ClientConfig clientConfig;
	private final ModIdFormattingConfig modIdFormattingConfig;
	private final IngredientFilterConfig ingredientFilterConfig;
	private final WorldConfig worldConfig;
	private final IModIdHelper modIdHelper;
	private final IHideModeConfig hideModeConfig;

	public ClientLifecycleHandler(NetworkHandler networkHandler) {
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
		worldConfig = new WorldConfig(jeiConfigurationDir);
		ingredientFilterConfig = new IngredientFilterConfig(clientConfig.getConfig());
		modIdFormattingConfig = new ModIdFormattingConfig(clientConfig.getConfig());
		modIdHelper = new ForgeModIdHelper(clientConfig, modIdFormattingConfig);
		ErrorUtil.setModIdHelper(modIdHelper);
		hideModeConfig = new HideModeConfig(modIdHelper, jeiConfigurationDir);

		KeyBindings.init();

//		eventBus.addListener(EventPriority.NORMAL, false, FMLLoadCompleteEvent.class, event -> this.onLoadComplete());
		EventBusHelper.addListener(GuiScreenEvent.KeyboardKeyPressedEvent.Pre.class, this::onGuiKeyPressedEvent);

		clientConfig.onPreInit();
		EventBusHelper.addListener(ConfigChangedEvent.OnConfigChangedEvent.class, event -> {
			modIdFormattingConfig.checkForModNameFormatOverride();
			if (ModIds.JEI_ID.equals(event.getModID())) {
				if (clientConfig.syncAllConfig()) {
					// todo
				}
				if (ingredientFilterConfig.syncConfig()) {
					JeiRuntime runtime = Internal.getRuntime();
					if (runtime != null) {
						IngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
						ingredientListOverlay.rebuildIngredientFilter();
					}
				}
				if (worldConfig.syncConfig()) {
					// todo
				}
			}
		});
		EventBusHelper.addListener(WorldEvent.Save.class, event -> worldConfig.onWorldSave());

		networkHandler.createClientPacketHandler(worldConfig);
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
				this.starter.start(plugins, clientConfig, hideModeConfig, ingredientFilterConfig, worldConfig, modIdHelper);
			}
		});

		this.starter.start(plugins, clientConfig, hideModeConfig, ingredientFilterConfig, worldConfig, modIdHelper);
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
				worldConfig.syncWorldConfig(networkManager);
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
