package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJei;

public interface IConnectionToServer {
	boolean isJeiOnServer();

	/**
	 * Returns true when the connected server is using the same mod loader as the client.
	 */
	boolean isSameModLoader();

	void sendPacketToServer(PacketJei packet);

	default void onRuntimeStopped() {

	}
}
