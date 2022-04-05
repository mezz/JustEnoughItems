package mezz.jei.forge.startup;

import mezz.jei.common.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.config.BookmarkConfig;
import mezz.jei.common.config.EditModeConfig;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.forge.config.ForgeKeyBindings;
import mezz.jei.forge.config.JEIClientConfigs;
import mezz.jei.forge.config.ModIdFormattingConfig;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.common.config.sorting.ModNameSortingConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.events.RuntimeEventSubscriptions;
import mezz.jei.forge.network.ConnectionToServer;
import mezz.jei.forge.network.NetworkHandler;
import mezz.jei.forge.util.AnnotatedInstanceUtil;
import mezz.jei.forge.util.ForgeRecipeRegistryHelper;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.forge.helpers.ForgeModIdHelper;
import mezz.jei.common.ingredients.IIngredientSorter;
import mezz.jei.common.ingredients.IngredientSorter;
import mezz.jei.startup.JeiStarter;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RecipeErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final JeiStarter jeiStarter;
	private final ModIdFormattingConfig modIdFormattingConfig;
	private final StartEventObserver startEventObserver = new StartEventObserver(this::startJei, this::stopJei);
	private final RuntimeEventSubscriptions runtimeSubscriptions;

	public ClientLifecycleHandler(NetworkHandler networkHandler, Textures textures, JEIClientConfigs jeiClientConfigs, IServerConfig serverConfig) {
		this.runtimeSubscriptions = new RuntimeEventSubscriptions(MinecraftForge.EVENT_BUS);
		File jeiConfigurationDir = createConfigDir();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		this.modIdFormattingConfig = jeiClientConfigs.getModNameFormat();
		IModIdHelper modIdHelper = new ForgeModIdHelper(clientConfig, this.modIdFormattingConfig);
		ErrorUtil.setModIdHelper(modIdHelper);
		RecipeErrorUtil.setModIdHelper(modIdHelper);
		RecipeErrorUtil.setRecipeRegistryHelper(new ForgeRecipeRegistryHelper());

		// Additional config files
		IBookmarkConfig bookmarkConfig = new BookmarkConfig(jeiConfigurationDir);
		IEditModeConfig editModeConfig = new EditModeConfig(jeiConfigurationDir);
		RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(new File(jeiConfigurationDir, "recipe-category-sort-order.ini"));

		IIngredientSorter ingredientSorter = createIngredientSorter(clientConfig, jeiConfigurationDir);

		IConnectionToServer serverConnection = new ConnectionToServer();
		Internal.setServerConnection(serverConnection);

		ForgeKeyBindings keyBindings = new ForgeKeyBindings();
		keyBindings.register();

		WorldConfig worldConfig = new WorldConfig(serverConnection, keyBindings);
		networkHandler.createClientPacketHandler(serverConnection, serverConfig, worldConfig);

		List<IModPlugin> plugins = AnnotatedInstanceUtil.getModPlugins();

		this.jeiStarter = new JeiStarter(
			plugins,
			textures,
			jeiClientConfigs,
			editModeConfig,
			worldConfig,
			serverConnection,
			bookmarkConfig,
			modIdHelper,
			recipeCategorySortingConfig,
			ingredientSorter,
			keyBindings
		);
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		this.startEventObserver.register(subscriptions);
	}

	public PreparableReloadListener getReloadListener() {
		return this.startEventObserver;
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
		LOGGER.info("Stopping JEI");
		this.runtimeSubscriptions.clear();
		Internal.setRuntime(null);
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
