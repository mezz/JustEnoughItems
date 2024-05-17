package mezz.jei.common.network;

import mezz.jei.common.network.packets.PlayToServerPacket;

public interface IConnectionToServer {
	boolean isJeiOnServer();

	<T extends PlayToServerPacket<T>> void sendPacketToServer(T packet);
}
