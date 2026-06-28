package mezz.jei.neoforge.tests.lib;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public final class TestConnectionToServer implements IConnectionToServer {
	private static final IServerConfig SERVER_CONFIG = new TestServerConfig();
	private static final IConnectionToClient CONNECTION_TO_CLIENT = new TestConnectionToClient();

	@Nullable
	private ServerPlayer player;

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
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		ServerPlayer player = this.player;
		if (player == null) {
			throw new IllegalStateException("No active player for recipe transfer packet");
		}
		packet.process(new ServerPacketContext(player, SERVER_CONFIG, CONNECTION_TO_CLIENT));
	}

	@Override
	public void onRuntimeStopped() {
		clearPlayer();
	}
}
