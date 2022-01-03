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
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ClientLifecycleHandler {
	private static final Logger LOGGER = LogManager.getLogger();

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
	private final File jeiConfigurationDir;

	public ClientLifecycleHandler(NetworkHandler networkHandler, Textures textures) {
		jeiConfigurationDir = new File(FMLPaths.CONFIGDIR.get().toFile(), ModIds.JEI_ID);
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

		if (Internal.getReloadListener() == null) {
			//Should never be null as we set it in an earlier event
			throw new RuntimeException("Something went wrong when registering JEI's reload listener.");
		}
		Internal.getReloadListener().update(this);

		EventBusHelper.addListener(this, WorldEvent.Save.class, event -> worldConfig.onWorldSave());

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

	public void setupJEI() {
		worldConfig.syncWorldConfig(jeiConfigurationDir);

		modIdFormattingConfig.checkForModNameFormatOverride();

		startJEI();
		EventBusHelper.post(new PlayerJoinedWorldEvent());
	}

	public void startJEI() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			LOGGER.error("Failed to start JEI, there is no Minecraft client level.");
			return;
		}
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

	private enum ServerType {
		// Two cases, one for first connection and one for when it is an integrated server or  cases as both vanilla and modded servers share the same post reload handling
		// and integrated servers always have recipes update after tags
		FIRST_CONNECTION(TagsUpdatedEvent.class),
		INTEGRATED_OR_POST_RELOAD(RecipesUpdatedEvent.class);

		public boolean hasRan;
		public final Class<? extends Event> listenerClass;

		ServerType(Class<? extends Event> listenerClass) {
			this.listenerClass = listenerClass;
		}

		public boolean shouldRun() {
			Minecraft minecraft = Minecraft.getInstance();
			ClientPacketListener connection = minecraft.getConnection();
			boolean isIntegrated = minecraft.isLocalServer();
			if (connection == null || isIntegrated) {
				//If we are an integrated server we always handle recipes updating as it is consistently last,
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
				// If we haven't ran this is the first time the event is being fired,
				// so we mark that it has ran but don't actually run it again
				hasRan = true;
				return false;
			} else if (hasRan) {
				//Non post reloads only run the first time around
				return false;
			}
			hasRan = true;
			return true;
		}
	}
}
