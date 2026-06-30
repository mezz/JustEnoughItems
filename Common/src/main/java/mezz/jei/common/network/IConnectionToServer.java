package mezz.jei.common.network;

import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IConnectionToServer {
	boolean isJeiOnServer();

	boolean canSendPacket(CustomPacketPayload.Type<?> packetType);

	<T extends PlayToServerPacket<T>> void sendPacketToServer(T packet);

	void onRuntimeStopped();
}
