package mezz.jei.forge.tests.lib;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.ServerPacketRouter;
import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public final class TestConnectionToServer implements IConnectionToServer {
	private static final IServerConfig SERVER_CONFIG = new TestServerConfig();
	private static final IConnectionToClient CONNECTION_TO_CLIENT = new TestConnectionToClient();
	private static final ServerPacketRouter PACKET_ROUTER = new ServerPacketRouter(CONNECTION_TO_CLIENT, SERVER_CONFIG);

	private final boolean sameModLoader;

	@Nullable
	private ServerPlayer player;

	public TestConnectionToServer() {
		this(true);
	}

	public TestConnectionToServer(boolean sameModLoader) {
		this.sameModLoader = sameModLoader;
	}

	public void setPlayer(ServerPlayer player) {
		this.player = player;
	}

	public void clearPlayer() {
		this.player = null;
	}

	@Override
	public boolean isJeiOnServer() {
		return true;
	}

	@Override
	public boolean isSameModLoader() {
		return sameModLoader;
	}

	@Override
	public void sendPacketToServer(PacketJei packet) {
		ServerPlayer player = this.player;
		if (player == null) {
			throw new IllegalStateException("No active player for recipe transfer packet");
		}
		Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
		PACKET_ROUTER.onPacket(packetData.getLeft(), player);
	}

	public void sendPacketDataToServer(FriendlyByteBuf packetData) {
		ServerPlayer player = this.player;
		if (player == null) {
			throw new IllegalStateException("No active player for recipe transfer packet");
		}
		PACKET_ROUTER.onPacket(packetData, player);
	}

	@Override
	public void onRuntimeStopped() {
		clearPlayer();
	}
}
