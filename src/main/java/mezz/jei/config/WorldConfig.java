package mezz.jei.config;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.network.NetworkManager;

import mezz.jei.JustEnoughItems;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Translator;

/** Owns the settings and saved search text for the current world or server. */
public class WorldConfig implements IWorldConfig {
	private final WorldConfigValues defaultValues = new WorldConfigValues();
	private final WorldConfigValues values = new WorldConfigValues();
	private final Supplier<String> worldUid;
	@Nullable
	private Configuration worldConfig;

	public WorldConfig() {
		this.worldUid = () -> ServerInfo.getWorldUid(FMLClientHandler.instance().getClientToServerNetworkManager());
	}

	public WorldConfig(Configuration worldConfig, Supplier<String> worldUid) {
		this.worldConfig = worldConfig;
		this.worldUid = worldUid;
	}

	public void setConfiguration(Configuration worldConfig) {
		this.worldConfig = worldConfig;
	}

	@Nullable
	public Configuration getConfig() {
		return worldConfig;
	}

	public boolean syncWorldConfig(@Nullable NetworkManager networkManager) {
		if (worldConfig == null) {
			return false;
		}
		return syncWorldConfigForWorld(ServerInfo.getWorldUid(networkManager));
	}

	@Override
	public boolean isOverlayEnabled() {
		return values.overlayEnabled ||
			KeyBindings.toggleOverlay.getKeyCode() == 0; // if there is no key binding to enable it, don't allow the overlay to be disabled
	}

	@Override
	public void toggleOverlayEnabled() {
		values.overlayEnabled = !values.overlayEnabled;

		if (worldConfig != null) {
			final String worldCategory = worldUid.get();
			Property property = worldConfig.get(worldCategory, "overlayEnabled", defaultValues.overlayEnabled);
			property.set(values.overlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new OverlayToggleEvent(values.overlayEnabled));
	}

	@Override
	public boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && values.bookmarkOverlayEnabled;
	}

	@Override
	public void toggleBookmarkEnabled() {
		values.bookmarkOverlayEnabled = !values.bookmarkOverlayEnabled;

		if (worldConfig != null) {
			final String worldCategory = worldUid.get();
			Property property = worldConfig.get(worldCategory, "bookmarkOverlayEnabled", defaultValues.bookmarkOverlayEnabled);
			property.set(values.bookmarkOverlayEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new BookmarkOverlayToggleEvent(values.bookmarkOverlayEnabled));
	}

	@Override
	public boolean isDeleteItemsInCheatModeActive() {
		return values.cheatItemsEnabled && ServerInfo.isJeiOnServer();
	}

	@Override
	public boolean isCheatItemsEnabled() {
		return values.cheatItemsEnabled;
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
				final String worldCategory = worldUid.get();
				Property property = worldConfig.get(worldCategory, "cheatItemsEnabled", defaultValues.cheatItemsEnabled);
				property.set(values.cheatItemsEnabled);

				if (worldConfig.hasChanged()) {
					worldConfig.save();
				}
			}

			if (values.cheatItemsEnabled && ServerInfo.isJeiOnServer()) {
				JustEnoughItems.getProxy().sendPacketToServer(new PacketRequestCheatPermission());
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
			final String worldCategory = worldUid.get();
			Property property = worldConfig.get(worldCategory, "editEnabled", defaultValues.editModeEnabled);
			property.set(values.editModeEnabled);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}

		MinecraftForge.EVENT_BUS.post(new EditModeToggleEvent(values.editModeEnabled));
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
	public String getFilterText() {
		return values.filterText;
	}

	@Override
	public void saveFilterText() {
		if (worldConfig != null) {
			final String worldCategory = worldUid.get();
			Property property = worldConfig.get(worldCategory, "filterText", defaultValues.filterText);
			property.set(values.filterText);

			if (worldConfig.hasChanged()) {
				worldConfig.save();
			}
		}
	}

	public boolean syncWorldConfigForWorld(String worldCategory) {
		if (worldConfig == null) {
			return false;
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
		return false;
	}
}
