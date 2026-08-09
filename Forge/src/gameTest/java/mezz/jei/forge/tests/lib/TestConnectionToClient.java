package mezz.jei.forge.tests.lib;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.server.level.ServerPlayer;

final class TestConnectionToClient implements IConnectionToClient {
	@Override
	public void sendPacketToClient(PacketJei packet, ServerPlayer player) {
	}
}
