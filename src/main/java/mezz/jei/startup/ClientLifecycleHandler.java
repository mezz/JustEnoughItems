package mezz.jei.startup;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import mezz.jei.config.*;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;

import com.google.common.base.Preconditions;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.overlay.IngredientListOverlay;
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
	private final Textures textures;
	private final ClientConfig clientConfig;
	private final BookmarkConfig bookmarkConfig;
	private final ModIdFormattingConfig modIdFormattingConfig;
	private final IngredientFilterConfig ingredientFilterConfig;
	private final WorldConfig worldConfig;
	private final IModIdHelper modIdHelper;
	private final IEditModeConfig editModeConfig;

	public ClientLifecycleHandler(NetworkHandler networkHandler, Textures textures) {
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

		this.clientConfig = JEIClientConfig.clientConfig;
		this.ingredientFilterConfig = JEIClientConfig.filterConfig;
		this.modIdFormattingConfig = JEIClientConfig.modNameFormat;
		this.modIdHelper = new ForgeModIdHelper(clientConfig, modIdFormattingConfig);

		// Additional config files
		bookmarkConfig = new BookmarkConfig(jeiConfigurationDir);
		worldConfig = new WorldConfig(jeiConfigurationDir);
		editModeConfig = new EditModeConfig(jeiConfigurationDir);

		ErrorUtil.setModIdHelper(modIdHelper);
		ErrorUtil.setWorldConfig(worldConfig);

		KeyBindings.init();

		EventBusHelper.addListener(WorldEvent.Save.class, event -> worldConfig.onWorldSave());
		EventBusHelper.addListener(RecipesUpdatedEvent.class, event -> {
			ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				NetworkManager networkManager = connection.getNetworkManager();
				worldConfig.syncWorldConfig(networkManager);
			}
			onRecipesLoaded();
			EventBusHelper.post(new PlayerJoinedWorldEvent());
		});

		networkHandler.createClientPacketHandler(worldConfig);

		this.textures = textures;
	}

	private void onRecipesLoaded() {
		modIdFormattingConfig.checkForModNameFormatOverride();

		List<IModPlugin> plugins = AnnotatedInstanceUtil.getModPlugins();

		// Reload when resources change
		Minecraft minecraft = Minecraft.getInstance();
		IResourceManager resourceManager = minecraft.getResourceManager();
		if (resourceManager instanceof IReloadableResourceManager) {
			IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) resourceManager;
			reloadableResourceManager.addReloadListener(new JeiReloadListener(plugins));
		}
		if (minecraft.world != null) {
			Preconditions.checkNotNull(textures);
			this.starter.start(plugins, textures, clientConfig, editModeConfig, ingredientFilterConfig, worldConfig, bookmarkConfig, modIdHelper);
		}
	}

	private final class JeiReloadListener implements ISelectiveResourceReloadListener {
		private final List<IModPlugin> plugins;

		private JeiReloadListener(List<IModPlugin> plugins) {
			this.plugins = plugins;
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
			// check that JEI has been started before. if not, do nothing
			if (starter.hasStarted() && Minecraft.getInstance().world != null) {
				LOGGER.info("Restarting JEI.");
				Preconditions.checkNotNull(textures);
				starter.start(plugins, textures, clientConfig, editModeConfig, ingredientFilterConfig, worldConfig, bookmarkConfig, modIdHelper);
			}
		}
	}
}
