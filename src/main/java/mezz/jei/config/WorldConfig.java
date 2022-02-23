package mezz.jei.config;

import mezz.jei.config.forge.Configuration;
import mezz.jei.config.forge.Property;
import mezz.jei.events.BookmarkOverlayToggleEvent;
import mezz.jei.events.EditModeToggleEvent;
import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Translator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.nio.file.Path;

public class WorldConfig implements IWorldConfig, IFilterTextSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String worldCategory = "world";
	private final WorldConfigValues defaultValues = new WorldConfigValues();
	private final WorldConfigValues values = new WorldConfigValues();
	private final File jeiConfigurationDir;
	@Nullable
	private Configuration worldConfig;

	@Nullable
	private static Configuration getConfiguration(File jeiConfigurationDir) {
		Path configPath = ServerInfo.getWorldPath(jeiConfigurationDir.toPath());
		if (configPath == null) {
			return null;
		}
		Path worldConfigPath = configPath.resolve("worldSettings.cfg");
		return new Configuration(worldConfigPath.toFile());
	}

	public WorldConfig(File jeiConfigurationDir) {
		this.worldConfig = getConfiguration(jeiConfigurationDir);
		this.jeiConfigurationDir = jeiConfigurationDir;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(WorldEvent.Save.class, event -> onWorldSave());
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
		if (worldConfig != null) {
			Property property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
			property.set(values.filterText);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}
	}

	@Override
	public boolean isOverlayEnabled() {
		return values.overlayEnabled ||
			KeyBindings.toggleOverlay.getKey().getValue() == GLFW.GLFW_KEY_UNKNOWN; // if there is no key binding to enable it, don't allow the overlay to be disabled
	}

	@Override
	public void toggleOverlayEnabled() {
		values.overlayEnabled = !values.overlayEnabled;

		if (worldConfig != null) {
			Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
			property.set(values.overlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}
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
			if (worldConfig != null) {
				Property property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
				property.set(values.bookmarkOverlayEnabled);

				if (worldConfig.hasChanged()) {
					worldConfig.save();
				}
			}

			MinecraftForge.EVENT_BUS.post(new BookmarkOverlayToggleEvent(values.bookmarkOverlayEnabled));
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

			if (worldConfig != null) {
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
				property.set(values.cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					worldConfig.save();
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

		if (worldConfig != null) {
			Property property = worldConfig.get(worldCategory, "editModeEnabled", defaultValues.editModeEnabled);
			property.set(values.editModeEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
				MinecraftForge.EVENT_BUS.post(new EditModeToggleEvent(values.editModeEnabled));
			}
		}
	}

	private void onWorldSave() {
		try {
			saveFilterText();
		} catch (RuntimeException e) {
			LOGGER.error("Failed to save filter text.", e);
		}
	}

	public void syncWorldConfig() {
		worldConfig = getConfiguration(jeiConfigurationDir);
		if (worldConfig == null) {
			return;
		}

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
			MinecraftForge.EVENT_BUS.post(new EditModeToggleEvent(values.editModeEnabled));
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
			worldConfig.save();
		}
	}

}
