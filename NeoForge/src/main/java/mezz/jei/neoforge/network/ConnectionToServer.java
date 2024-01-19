package mezz.jei.neoforge.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.packets.PacketJeiToServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectionToServer implements IConnectionToServer {
	@Nullable
	private static UUID jeiOnServerCacheUuid = null;
	private static boolean jeiOnServerCacheValue = false;

	@Override
	public boolean isJeiOnServer() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null) {
			return false;
		}
		UUID id = clientPacketListener.getId();
		if (!id.equals(jeiOnServerCacheUuid)) {
			jeiOnServerCacheUuid = id;
			jeiOnServerCacheValue = clientPacketListener.isConnected(NetworkHandler.toServerID(PacketIdServer.DELETE_ITEM));
		}
		return jeiOnServerCacheValue;
	}

	@Override
	public void sendPacketToServer(PacketJeiToServer packet) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener netHandler = minecraft.getConnection();
		if (netHandler != null && isJeiOnServer()) {
			ResourceLocation id = NetworkHandler.toServerID(packet.getPacketId());
			PacketDistributor.SERVER.noArg().send(new WrappingPayload<>(packet, id));
		}
	}
}
