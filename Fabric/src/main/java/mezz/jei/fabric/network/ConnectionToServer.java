package mezz.jei.fabric.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ConnectionToServer implements IConnectionToServer {
	@Override
	public boolean isJeiOnServer() {
		return ClientPlayNetworking.canSend(PacketDeletePlayerItem.TYPE);
	}

	@Override
	public boolean canSendPacket(CustomPacketPayload.Type<?> packetType) {
		return ClientPlayNetworking.canSend(packetType);
	}

	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		if (canSendPacket(packet.type())) {
			ClientPlayNetworking.send(packet);
		}
	}

	@Override
	public void onRuntimeStopped() {

	}
}
