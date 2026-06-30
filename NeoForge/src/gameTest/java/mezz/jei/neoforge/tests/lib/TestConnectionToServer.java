package mezz.jei.neoforge.tests.lib;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class TestConnectionToServer implements IConnectionToServer {
	private static final IServerConfig SERVER_CONFIG = new TestServerConfig();
	private static final IConnectionToClient CONNECTION_TO_CLIENT = new TestConnectionToClient();

	private final Set<CustomPacketPayload.Type<?>> unsupportedPackets = new HashSet<>();

	@Nullable
	private ServerPlayer player;

	public void addUnsupportedPacket(CustomPacketPayload.Type<?> packetType) {
		this.unsupportedPackets.add(packetType);
	}

	public void setPlayer(ServerPlayer player) {
		this.player = player;
	}

	public void clearPlayer() {
		this.player = null;
	}

	@Override
	public boolean isJeiOnServer() {
		return canSendPacket(PacketDeletePlayerItem.TYPE);
	}

	@Override
	public boolean canSendPacket(CustomPacketPayload.Type<?> packetType) {
		return !this.unsupportedPackets.contains(packetType);
	}

	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		if (!canSendPacket(packet.type())) {
			throw new IllegalStateException("Server does not support packet %s".formatted(packet.type().id()));
		}
		ServerPlayer player = this.player;
		if (player == null) {
			throw new IllegalStateException("No active player for recipe transfer packet");
		}
		T decodedPacket = encodeAndDecode(player, packet);
		decodedPacket.process(new ServerPacketContext(player, SERVER_CONFIG, CONNECTION_TO_CLIENT));
	}

	private static <T extends PlayToServerPacket<T>> T encodeAndDecode(ServerPlayer player, T packet) {
		ByteBuf byteBuf = Unpooled.buffer();
		try {
			RegistryFriendlyByteBuf buffer = RegistryFriendlyByteBuf.decorator(player.level().registryAccess(), ConnectionType.OTHER).apply(byteBuf);
			packet.streamCodec().encode(buffer, packet);
			return packet.streamCodec().decode(buffer);
		} finally {
			byteBuf.release();
		}
	}

	@Override
	public void onRuntimeStopped() {
		clearPlayer();
	}
}
