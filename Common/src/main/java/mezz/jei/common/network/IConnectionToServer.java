package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJeiToServer;

public interface IConnectionToServer {
    boolean isJeiOnServer();

    void sendPacketToServer(PacketJeiToServer packet);
}
