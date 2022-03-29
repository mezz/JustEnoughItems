package mezz.jei.config;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import org.lwjgl.glfw.GLFW;

public class WorldConfig implements IWorldConfig {
	private final IConnectionToServer serverConnection;
	private boolean overlayEnabled = true;
	private boolean cheatItemsEnabled = false;
	private boolean editModeEnabled = false;
	private boolean bookmarkOverlayEnabled = true;

	public WorldConfig(IConnectionToServer serverConnection) {
		this.serverConnection = serverConnection;
	}

	@Override
	public boolean isOverlayEnabled() {
		return overlayEnabled ||
			KeyBindings.toggleOverlay.getKey().getValue() == GLFW.GLFW_KEY_UNKNOWN; // if there is no key binding to enable it, don't allow the overlay to be disabled
	}

	@Override
	public void toggleOverlayEnabled() {
		this.overlayEnabled = !this.overlayEnabled;
	}

	@Override
	public boolean isBookmarkOverlayEnabled() {
		return isOverlayEnabled() && bookmarkOverlayEnabled;
	}

	@Override
	public void toggleBookmarkEnabled() {
		setBookmarkEnabled(!bookmarkOverlayEnabled);
	}

	@Override
	public void setBookmarkEnabled(boolean value) {
		if (this.bookmarkOverlayEnabled != value) {
			this.bookmarkOverlayEnabled = value;
		}
	}

	@Override
	public boolean isCheatItemsEnabled() {
		return cheatItemsEnabled;
	}

	@Override
	public boolean isDeleteItemsInCheatModeActive() {
		return cheatItemsEnabled && serverConnection.isJeiOnServer();
	}


	@Override
	public void toggleCheatItemsEnabled() {
		setCheatItemsEnabled(!cheatItemsEnabled);
	}

	@Override
	public void setCheatItemsEnabled(boolean value) {
		if (cheatItemsEnabled != value) {
			cheatItemsEnabled = value;
			if (cheatItemsEnabled && serverConnection.isJeiOnServer()) {
				serverConnection.sendPacketToServer(new PacketRequestCheatPermission());
			}
		}
	}

	@Override
	public boolean isEditModeEnabled() {
		return editModeEnabled;
	}

	@Override
	public void toggleEditModeEnabled() {
		this.editModeEnabled = !this.editModeEnabled;
	}
}
