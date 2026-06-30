package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJei;

public interface IConnectionToServer {
	boolean isJeiOnServer();

	boolean isSameModLoader();

	void sendPacketToServer(PacketJei packet);

	default void onRuntimeStopped() {
	}
}
