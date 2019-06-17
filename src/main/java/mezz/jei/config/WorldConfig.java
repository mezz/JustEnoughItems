package mezz.jei.config;

import javax.annotation.Nullable;
import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.NetworkManager;

import mezz.jei.config.forge.Configuration;
import mezz.jei.config.forge.Property;
import mezz.jei.events.BookmarkOverlayToggleEvent;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.EventBusHelper;
import mezz.jei.events.OverlayToggleEvent;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Translator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class WorldConfig implements IWorldConfig, IFilterTextSource {
	private static final Logger LOGGER = LogManager.getLogger();

	private final WorldConfigValues defaultValues = new WorldConfigValues();
	private final WorldConfigValues values = new WorldConfigValues();
	private final Configuration worldConfig;

	public WorldConfig(File jeiConfigurationDir) {
		// TODO move world settings into the world save folder
		final File worldConfigFile = new File(jeiConfigurationDir, "worldSettings.cfg");
		worldConfig = new Configuration(worldConfigFile);
	}

	@Override
	public String getFilterText() {
		return values.filterText;
	}

	@Override
	public boolean setFilterText(String filterText) {
		if (values.filterText.equals(filterText)) {
			return false;
		} else {
			values.filterText = filterText;
			return true;
		}
	}

	@Override
	public void saveFilterText() {
		ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
			property.set(values.filterText);

			if (worldConfig.hasChanged()) {
				// TODO 1.13
//				worldConfig.save();
			}
		}
	}

	@Override
	public boolean isOverlayEnabled() {
		return values.overlayEnabled ||
			KeyBindings.toggleOverlay.getKey().getKeyCode() == GLFW.GLFW_KEY_UNKNOWN; // if there is no key binding to enable it, don't allow the overlay to be disabled
	}

	@Override
	public void toggleOverlayEnabled() {
		values.overlayEnabled = !values.overlayEnabled;

		ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
			property.set(values.overlayEnabled);

			if (worldConfig.hasChanged()) {
				// TODO 1.13
//				worldConfig.save();
			}
		}

		EventBusHelper.post(new OverlayToggleEvent(values.overlayEnabled));
	}

	@Override
	public boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && values.bookmarkOverlayEnabled;
	}

	@Override
	public void toggleBookmarkEnabled() {
		setBookmarkEnabled(!values.bookmarkOverlayEnabled);
	}

	@Override
	public void setBookmarkEnabled(boolean value) {
		if (values.bookmarkOverlayEnabled != value) {
			values.bookmarkOverlayEnabled = value;
			ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				NetworkManager networkManager = connection.getNetworkManager();
				final String worldCategory = ServerInfo.getWorldUid(networkManager);
				Property property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
				property.set(values.bookmarkOverlayEnabled);

				if (worldConfig.hasChanged()) {
					// TODO 1.13
	//				worldConfig.save();
				}
			}

			EventBusHelper.post(new BookmarkOverlayToggleEvent(values.bookmarkOverlayEnabled));
		}
	}

	@Override
	public boolean isCheatItemsEnabled() {
		return values.cheatItemsEnabled;
	}

	@Override
	public boolean isDeleteItemsInCheatModeActive() {
		return values.cheatItemsEnabled && ServerInfo.isJeiOnServer();
	}


	@Override
	public void toggleCheatItemsEnabled() {
		setCheatItemsEnabled(!values.cheatItemsEnabled);
	}

	@Override
	public void setCheatItemsEnabled(boolean value) {
		if (values.cheatItemsEnabled != value) {
			values.cheatItemsEnabled = value;

			ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				NetworkManager networkManager = connection.getNetworkManager();
				final String worldCategory = ServerInfo.getWorldUid(networkManager);
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
				property.set(values.cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					// TODO 1.13
//					worldConfig.save();
				}
			}

			if (values.cheatItemsEnabled && ServerInfo.isJeiOnServer()) {
				Network.sendPacketToServer(new PacketRequestCheatPermission());
			}
		}
	}

	@Override
	public boolean isEditModeEnabled() {
		return values.editModeEnabled;
	}

	@Override
	public void toggleEditModeEnabled() {
		values.editModeEnabled = !values.editModeEnabled;

		ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			final String worldCategory = ServerInfo.getWorldUid(networkManager);
			Property property = worldConfig.get(worldCategory, "editModeEnabled", defaultValues.editModeEnabled);
			property.set(values.editModeEnabled);

			if (worldConfig.hasChanged()) {
				// TODO 1.13
//					worldConfig.save();
			}
		}
	}

	public void onWorldSave() {
		try {
			saveFilterText();
		} catch (RuntimeException e) {
			LOGGER.error("Failed to save filter text.", e);
		}
	}

	public boolean syncConfig() {
		ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			NetworkManager networkManager = connection.getNetworkManager();
			return syncWorldConfig(networkManager);
		}
		return false;
	}

	public boolean syncWorldConfig(@Nullable NetworkManager networkManager) {
		final String worldCategory = ServerInfo.getWorldUid(networkManager);

		Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
		property.setLanguageKey("config.jei.interface.overlayEnabled");
		property.setComment(Translator.translateToLocal("config.jei.interface.overlayEnabled.comment"));
		property.setShowInGui(false);
		values.overlayEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
		property.setLanguageKey("config.jei.mode.cheatItemsEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.cheatItemsEnabled.comment"));
		values.cheatItemsEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "editEnabled", defaultValues.editModeEnabled);
		property.setLanguageKey("config.jei.mode.editEnabled");
		property.setComment(Translator.translateToLocal("config.jei.mode.editEnabled.comment"));
		values.editModeEnabled = property.getBoolean();
		if (property.hasChanged()) {
			EventBusHelper.post(new EditModeToggleEvent(values.editModeEnabled));
		}

		property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
		property.setLanguageKey("config.jei.interface.bookmarkOverlayEnabled");
		property.setComment(Translator.translateToLocal("config.jei.interface.bookmarkOverlayEnabled.comment"));
		property.setShowInGui(false);
		values.bookmarkOverlayEnabled = property.getBoolean();

		property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
		property.setShowInGui(false);
		values.filterText = property.getString();

		final boolean configChanged = worldConfig.hasChanged();
		if (configChanged) {
			// TODO 1.13
//			worldConfig.save();
		}
		return false;
	}

}
