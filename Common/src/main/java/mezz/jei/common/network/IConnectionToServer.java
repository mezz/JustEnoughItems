package mezz.jei.common.network;

import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IConnectionToServer {
	boolean isJeiOnServer();

	/**
	 * Returns true when the connected server is using the same mod loader as the client.
	 */
	boolean isSameModLoader();

	boolean canSendPacket(CustomPacketPayload.Type<?> packetType);

	<T extends PlayToServerPacket<T>> void sendPacketToServer(T packet);

	default void onRuntimeStopped() {

	}
}
