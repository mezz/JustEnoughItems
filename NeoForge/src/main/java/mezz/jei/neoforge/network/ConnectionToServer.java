package mezz.jei.neoforge.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ConnectionToServer implements IConnectionToServer {
	private static JeiServerState jeiOnServerCacheValue = JeiServerState.UNKNOWN;

	private enum JeiServerState {
		UNKNOWN, ON_SERVER, NOT_ON_SERVER
	}

	@Override
	public boolean isJeiOnServer() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null || !clientPacketListener.getConnection().isConnected()) {
			jeiOnServerCacheValue = JeiServerState.UNKNOWN;
			return false;
		}
		if (jeiOnServerCacheValue == JeiServerState.UNKNOWN) {
			boolean onServer = clientPacketListener.hasChannel(PacketDeletePlayerItem.TYPE);
			jeiOnServerCacheValue = onServer ? JeiServerState.ON_SERVER : JeiServerState.NOT_ON_SERVER;
		}
		return jeiOnServerCacheValue == JeiServerState.ON_SERVER;
	}

	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener netHandler = minecraft.getConnection();
		if (netHandler != null && isJeiOnServer()) {
			ClientPacketDistributor.sendToServer(packet);
		}
	}

	@Override
	public void onRuntimeStopped() {
		jeiOnServerCacheValue = JeiServerState.UNKNOWN;
	}
}
