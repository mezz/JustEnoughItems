package mezz.jei.startup;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IReloadableResourceManager;

import com.google.common.base.Preconditions;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ModIds;
import mezz.jei.api.ingredients.IModIdHelper;
import mezz.jei.config.BookmarkConfig;
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
import mezz.jei.gui.textures.JeiTextureMap;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.ForgeModIdHelper;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientLifecycleHandler {
	private final Logger LOGGER = LogManager.getLogger();
	private final JeiStarter starter = new JeiStarter();
	private final JeiTextureMap textureMap = new JeiTextureMap("textures");
	@Nullable
	private Textures textures;
	private final ClientConfig clientConfig;
	private final BookmarkConfig bookmarkConfig;
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
		bookmarkConfig = new BookmarkConfig(jeiConfigurationDir);
		worldConfig = new WorldConfig(jeiConfigurationDir);
		ingredientFilterConfig = new IngredientFilterConfig(clientConfig.getConfig());
		modIdFormattingConfig = new ModIdFormattingConfig(clientConfig.getConfig());
		modIdHelper = new ForgeModIdHelper(clientConfig, modIdFormattingConfig);
		ErrorUtil.setModIdHelper(modIdHelper);
		hideModeConfig = new HideModeConfig(jeiConfigurationDir);

		KeyBindings.init();

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
		EventBusHelper.addListener(TextureStitchEvent.Pre.class, event -> {
			textures = new Textures(textureMap);
		});
		EventBusHelper.addListener(RecipesUpdatedEvent.class, event -> {
			NetHandlerPlayClient connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				NetworkManager networkManager = connection.getNetworkManager();
				worldConfig.syncWorldConfig(networkManager);
			}
			onRecipesLoaded();
			EventBusHelper.post(new PlayerJoinedWorldEvent());
		});

		networkHandler.createClientPacketHandler(worldConfig);
	}

	public void onLoadComplete() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.addScheduledTask(() -> {
			minecraft.textureManager.loadTickableTexture(textureMap.getLocation(), textureMap);
		});
	}

	private void onRecipesLoaded() {
		modIdFormattingConfig.checkForModNameFormatOverride();

		List<IModPlugin> plugins = AnnotatedInstanceUtil.getModPlugins();

		// Reload when resources change
		Minecraft minecraft = Minecraft.getInstance();
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
		reloadableResourceManager.addReloadListener(resourceManager -> {
			// check that JEI has been started before. if not, do nothing
			if (this.starter.hasStarted()) {
				LOGGER.info("Restarting JEI.");
				Preconditions.checkNotNull(textures);
				this.starter.start(plugins, textures, clientConfig, hideModeConfig, ingredientFilterConfig, worldConfig, bookmarkConfig, modIdHelper);
			}
		});

		Preconditions.checkNotNull(textures);
		this.starter.start(plugins, textures, clientConfig, hideModeConfig, ingredientFilterConfig, worldConfig, bookmarkConfig, modIdHelper);
	}
}
