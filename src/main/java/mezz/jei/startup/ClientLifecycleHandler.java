package mezz.jei.startup;

import com.google.common.base.Preconditions;
import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.config.BookmarkConfig;
import mezz.jei.config.EditModeConfig;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IngredientFilterConfig;
import mezz.jei.config.JEIClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.config.ModIdFormattingConfig;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.config.WorldConfig;
import mezz.jei.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.PlayerJoinedWorldEvent;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.ForgeModIdHelper;
import mezz.jei.ingredients.IIngredientSorter;
import mezz.jei.ingredients.IngredientSorter;
import mezz.jei.util.AnnotatedInstanceUtil;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ClientLifecycleHandler {
	final Logger LOGGER = LogManager.getLogger();
	final JeiStarter starter = new JeiStarter();
	final Textures textures;
	final IClientConfig clientConfig;
	final BookmarkConfig bookmarkConfig;
	final ModIdFormattingConfig modIdFormattingConfig;
	final IngredientFilterConfig ingredientFilterConfig;
	final WorldConfig worldConfig;
	final IModIdHelper modIdHelper;
	final IEditModeConfig editModeConfig;
	final RecipeCategorySortingConfig recipeCategorySortingConfig;
	final IIngredientSorter ingredientSorter;
	final List<IModPlugin> plugins;


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
		recipeCategorySortingConfig = new RecipeCategorySortingConfig(new File(jeiConfigurationDir, "recipe-category-sort-order.ini"));

		ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(new File(jeiConfigurationDir, "ingredient-list-mod-sort-order.ini"));
		IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(new File(jeiConfigurationDir, "ingredient-list-type-sort-order.ini"));
		ingredientSorter = new IngredientSorter(clientConfig, ingredientModNameSortingConfig, ingredientTypeSortingConfig);

		ErrorUtil.setModIdHelper(modIdHelper);
		ErrorUtil.setWorldConfig(worldConfig);

		KeyBindings.init();

		EventBusHelper.addListener(this, WorldEvent.Save.class, event -> worldConfig.onWorldSave());
		EventBusHelper.addListener(this, AddReloadListenerEvent.class, event -> reloadListenerSetup());

		EventBusHelper.addListener(this, ClientPlayerNetworkEvent.LoggedOutEvent.class, event -> {
			for (ServerType type : ServerType.values()) {
				type.hasRan = false;
			}
		});
		for (ServerType type : ServerType.values()) {
			EventBusHelper.addListener(this, type.listenerClass, event -> {
				if (type.shouldRun()) {
					setupJEI();
				}
			});
		}
		plugins = AnnotatedInstanceUtil.getModPlugins();

		networkHandler.createClientPacketHandler(worldConfig);

		this.textures = textures;
	}

	private void reloadListenerSetup() {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (!(resourceManager instanceof ReloadableResourceManager)) {
			return;
		}
		if (Internal.getReloadListener() == null) {
			JeiReloadListener reloadListener = new JeiReloadListener(this);
			Internal.setReloadListener(reloadListener);
		} else {
			Internal.getReloadListener().update(this);
		}
		((ReloadableResourceManager) resourceManager).registerReloadListener(Internal.getReloadListener());
	}

	public void setupJEI() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			Connection networkManager = connection.getConnection();
			worldConfig.syncWorldConfig(networkManager);
		}

		modIdFormattingConfig.checkForModNameFormatOverride();

		startJEI();
		EventBusHelper.post(new PlayerJoinedWorldEvent());
	}

	public void startJEI() {
		if (Minecraft.getInstance().level != null) {
			Preconditions.checkNotNull(textures);
			starter.start(
				plugins,
				textures,
				clientConfig,
				editModeConfig,
				ingredientFilterConfig,
				worldConfig,
				bookmarkConfig,
				modIdHelper,
				recipeCategorySortingConfig,
				ingredientSorter
			);
		}
	}

	private enum ServerType {
		// Three cases as both vanilla and modded servers share the same post reload handling
		// and integrated servers always have recipes update after tags
		VANILLA(TagsUpdatedEvent.VanillaTagTypes.class),
		MODDED(TagsUpdatedEvent.CustomTagTypes.class),
		INTEGRATED_OR_POST_RELOAD(RecipesUpdatedEvent.class);

		public boolean hasRan;
		public final Class<? extends Event> listenerClass;

		ServerType(Class<? extends Event> listenerClass) {
			this.listenerClass = listenerClass;
		}

		public boolean shouldRun() {
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			boolean isIntegrated = Minecraft.getInstance().isLocalServer();
			if (connection == null || isIntegrated) {
				//If we are an integrated server we always handle handle recipes updating as it is consistently last
				// so we ignore the value of hasRan. Note we also check if the connection is null and treat is as
				// integrated as the connection is null when connecting to a single player world during the tag events
				hasRan = true;
				return this == INTEGRATED_OR_POST_RELOAD;
			} else if (this == INTEGRATED_OR_POST_RELOAD) {
				//For post reload handling, if we have already had the recipes updated event fired
				// then we know we are a reload and can return true for if we should run
				if (hasRan) {
					return true;
				}
				// If we haven't ran this is the first time the event is being fired so we mark that it has ran but don't actually run it again
				hasRan = true;
				return false;
			} else if (hasRan) {
				//Non post reloads only run the first time around
				return false;
			}
			hasRan = true;
			boolean isVanilla = NetworkHooks.isVanillaConnection(connection.getConnection());
			return isVanilla == (this == VANILLA);
		}
	}
}
