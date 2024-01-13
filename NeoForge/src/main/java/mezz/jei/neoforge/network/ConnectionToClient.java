package mezz.jei.neoforge.network;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PacketJeiToClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class ConnectionToClient implements IConnectionToClient {
	@Override
	public void sendPacketToClient(PacketJeiToClient packet, ServerPlayer player) {
		ResourceLocation id = NetworkHandler.toClientID(packet.getPacketId());
		PacketDistributor.PLAYER.with(player).send(new WrappingPayload<>(packet, id));
	}
}
