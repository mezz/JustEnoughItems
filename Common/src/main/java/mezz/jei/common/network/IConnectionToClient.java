package mezz.jei.common.network;

import mezz.jei.common.network.packets.PlayToClientPacket;
import net.minecraft.server.level.ServerPlayer;

public interface IConnectionToClient {
	<T extends PlayToClientPacket<T>> void sendPacketToClient(T packet, ServerPlayer player);
}
