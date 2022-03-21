package mezz.jei.startup;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.config.EditModeConfig;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.JEIClientConfigs;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.ModIdFormattingConfig;
import mezz.jei.config.WorldConfig;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.events.DebugRestartJeiEvent;
import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.events.RuntimeEventSubscriptions;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.ForgeModIdHelper;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientSorter;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ClientLifecycleHandler implements ResourceManagerReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private final ModIdFormattingConfig modIdFormattingConfig;
	private final StartEventObserver startEventObserver = new StartEventObserver(this::startJei, this::restartJei);
	private final RuntimeEventSubscriptions runtimeSubscriptions;

	public ClientLifecycleHandler(NetworkHandler networkHandler, Textures textures, JEIClientConfigs jeiClientConfigs) {
		this.runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);
		File jeiConfigurationDir = createConfigDir();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		this.modIdFormattingConfig = jeiClientConfigs.getModNameFormat();
		IModIdHelper modIdHelper = new ForgeModIdHelper(clientConfig, this.modIdFormattingConfig);
		ErrorUtil.setModIdHelper(modIdHelper);

		// Additional config files
		BookmarkConfig bookmarkConfig = new BookmarkConfig(jeiConfigurationDir);
		IEditModeConfig editModeConfig = new EditModeConfig(jeiConfigurationDir);
		RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(new File(jeiConfigurationDir, "recipe-category-sort-order.ini"));

		IIngredientSorter ingredientSorter = createIngredientSorter(clientConfig, jeiConfigurationDir);

		WorldConfig worldConfig = new WorldConfig();
		networkHandler.createClientPacketHandler(worldConfig);

		List<IModPlugin> plugins = AnnotatedInstanceUtil.getModPlugins();

		KeyBindings.init();

		this.jeiStarter = new JeiStarter(
			plugins,
			textures,
			jeiClientConfigs,
			editModeConfig,
			worldConfig,
			bookmarkConfig,
			modIdHelper,
			recipeCategorySortingConfig,
			ingredientSorter
		);
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		this.startEventObserver.register(subscriptions);

		subscriptions.register(DebugRestartJeiEvent.class, this::onRestartJeiEvent);

		subscriptions.register(ClientPlayerNetworkEvent.LoggedOutEvent.class, event -> {
			if (event.getPlayer() != null) {
				this.stopJei();
			}
		});
	}

	private void startJei() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}
		if (!this.runtimeSubscriptions.isEmpty()) {
			LOGGER.error("Failed to start JEI, it is already running.");
			return;
		}
		this.modIdFormattingConfig.checkForModNameFormatOverride();

		this.jeiStarter.start(this.runtimeSubscriptions);
	}

	private void stopJei() {
		if (this.runtimeSubscriptions.isEmpty()) {
			LOGGER.error("Tried to stop JEI but it is not running.");
		} else {
			LOGGER.info("Stopping JEI");
		}
		this.runtimeSubscriptions.clear();
		Internal.setRuntime(null);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		restartJei();
	}

	private void onRestartJeiEvent(DebugRestartJeiEvent event) {
		LOGGER.warn("Restarting JEI from DebugRestartJeiEvent", new Throwable("Restarting JEI from DebugRestartJeiEvent"));
		restartJei();
	}

	private void restartJei() {
		if (!this.runtimeSubscriptions.isEmpty()) {
			LOGGER.info("Restarting JEI");
			this.runtimeSubscriptions.clear();
			this.jeiStarter.start(this.runtimeSubscriptions);
		}
	}

	private static IIngredientSorter createIngredientSorter(IClientConfig clientConfig, File jeiConfigurationDir) {
		ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(new File(jeiConfigurationDir, "ingredient-list-mod-sort-order.ini"));
		IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(new File(jeiConfigurationDir, "ingredient-list-type-sort-order.ini"));
		return new IngredientSorter(clientConfig, ingredientModNameSortingConfig, ingredientTypeSortingConfig);
	}

	private static File createConfigDir() {
		File configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), ModIds.JEI_ID);
		if (!configDir.exists()) {
			try {
				if (!configDir.mkdir()) {
					throw new RuntimeException("Could not create config directory " + configDir);
				}
			} catch (SecurityException e) {
				throw new RuntimeException("Could not create config directory " + configDir, e);
			}
		}
		return configDir;
	}
}
