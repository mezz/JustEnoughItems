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
import mezz.jei.config.sorting.IngredientTreeSortingConfig;
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
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkHooks;
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
		File invTweaksConfigurationDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "InvTweaks");
		if (!jeiConfigurationDir.exists()) {
			try {
				if (!jeiConfigurationDir.mkdir()) {
					throw new RuntimeException("Could not create config directory " + jeiConfigurationDir);
				}
			} catch (SecurityException e) {
				throw new RuntimeException("Could not create config directory " + jeiConfigurationDir, e);
			}
		}
		if (!invTweaksConfigurationDir.exists()) {  //Todo:  Add config setting to intentionally use JEI tree file.
			//If there is no Inventory Tweaks folder, we will use our tree file instead, always.
			invTweaksConfigurationDir = jeiConfigurationDir;
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
		IngredientTreeSortingConfig ingredientTreeSortingConfig = new IngredientTreeSortingConfig(new File(invTweaksConfigurationDir, "InvTweaksTree.txt"));
		ingredientSorter = new IngredientSorter(clientConfig, ingredientModNameSortingConfig, ingredientTypeSortingConfig, ingredientTreeSortingConfig);

		ErrorUtil.setModIdHelper(modIdHelper);
		ErrorUtil.setWorldConfig(worldConfig);

		KeyBindings.init();

		EventBusHelper.addListener(this, WorldEvent.Save.class, event -> worldConfig.onWorldSave());
		EventBusHelper.addListener(this, AddReloadListenerEvent.class, event -> reloadListenerSetup());

		for (ServerType type : ServerType.values()) {
			EventBusHelper.addListener(this, type.listenerClass, event -> {
				if (type.connected()) {
					setupJEI();
				}
			});
		}
		plugins = AnnotatedInstanceUtil.getModPlugins();

		networkHandler.createClientPacketHandler(worldConfig);

		this.textures = textures;
	}

	private void reloadListenerSetup() {
		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (!(resourceManager instanceof IReloadableResourceManager)) {
			return;
		}
		if (Internal.getReloadListener() == null) {
			JeiReloadListener reloadListener = new JeiReloadListener(this);
			Internal.setReloadListener(reloadListener);
		} else {
			Internal.getReloadListener().update(this);
		}
		((IReloadableResourceManager) resourceManager).addReloadListener(Internal.getReloadListener());
	}

	public void setupJEI() {
		ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			worldConfig.syncWorldConfig(networkManager);
		}

		modIdFormattingConfig.checkForModNameFormatOverride();

		startJEI();
		EventBusHelper.post(new PlayerJoinedWorldEvent());
	}

	public void startJEI() {
		if (Minecraft.getInstance().world != null) {
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
		// Three cases, since there's no such thing as a vanilla integrated server
		INTEGRATED(false, true, RecipesUpdatedEvent.class),
		VANILLA_REMOTE(true, false, TagsUpdatedEvent.VanillaTagTypes.class),
		MODDED_REMOTE(false, false, TagsUpdatedEvent.CustomTagTypes.class);

		public final boolean isVanilla, isIntegrated;
		public final Class<? extends Event> listenerClass;

		ServerType(boolean isVanilla, boolean isIntegrated, Class<? extends Event> listenerClass) {
			this.isVanilla = isVanilla;
			this.isIntegrated = isIntegrated;
			this.listenerClass = listenerClass;
		}

		public boolean connected() {
			ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
			boolean isVanilla = connection != null && NetworkHooks.isVanillaConnection(connection.getNetworkManager());
			boolean isIntegrated = Minecraft.getInstance().isIntegratedServerRunning();
			return isVanilla == this.isVanilla && isIntegrated == this.isIntegrated;
		}
	}
}
