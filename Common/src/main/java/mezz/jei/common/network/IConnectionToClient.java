package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJeiToClient;
import net.minecraft.server.level.ServerPlayer;

public interface IConnectionToClient {
    void sendPacketToClient(PacketJeiToClient packet, ServerPlayer player);
}
