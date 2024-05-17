package mezz.jei.fabric.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ConnectionToServer implements IConnectionToServer {
	@Override
	public boolean isJeiOnServer() {
		return ClientPlayNetworking.canSend(PacketDeletePlayerItem.TYPE);
	}

	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		if (isJeiOnServer()) {
			ClientPlayNetworking.send(packet);
		}
	}
}
