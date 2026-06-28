package mezz.jei.neoforge.tests.lib;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PlayToClientPacket;
import net.minecraft.server.level.ServerPlayer;

final class TestConnectionToClient implements IConnectionToClient {
	@Override
	public <T extends PlayToClientPacket<T>> void sendPacketToClient(T packet, ServerPlayer player) {
	}
}
