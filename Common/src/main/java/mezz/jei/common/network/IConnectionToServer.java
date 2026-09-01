package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJei;

public interface IConnectionToServer {
	boolean isJeiOnServer();

	/**
	 * Returns true when the connected server is using the same mod loader as the client.
	 */
	boolean isSameModLoader();

	/**
	 * Returns true when the server supports reporting the result of recipe transfer packets.
	 */
	default boolean supportsRecipeTransferResults() {
		return false;
	}

	void sendPacketToServer(PacketJei packet);

	default void onRuntimeStopped() {

	}
}
