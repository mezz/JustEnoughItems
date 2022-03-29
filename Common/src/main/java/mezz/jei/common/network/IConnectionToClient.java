package mezz.jei.common.network;

import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.server.level.ServerPlayer;

public interface IConnectionToClient {
    void sendPacketToClient(PacketJei packet, ServerPlayer player);
}
