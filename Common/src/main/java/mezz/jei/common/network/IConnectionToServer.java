package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJei;

public interface IConnectionToServer {
    boolean isJeiOnServer();

    void sendPacketToServer(PacketJei packet);
}
