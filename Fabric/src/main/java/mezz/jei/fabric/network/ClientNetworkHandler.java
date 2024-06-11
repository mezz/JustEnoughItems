package mezz.jei.fabric.network;

import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketCheatPermission;
import mezz.jei.common.network.packets.PlayToClientPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import java.util.function.BiConsumer;

public final class ClientNetworkHandler {
	private ClientNetworkHandler() {}

	public static void registerClientPacketHandler(IConnectionToServer connection) {
		PayloadTypeRegistry.playC2S().register(PacketCheatPermission.TYPE, PacketCheatPermission.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(PacketCheatPermission.TYPE, PacketCheatPermission.STREAM_CODEC);

		ClientPlayNetworking.registerGlobalReceiver(PacketCheatPermission.TYPE, wrapClientHandler(connection, PacketCheatPermission::process));
	}

	private static <T extends PlayToClientPacket<T>> ClientPlayNetworking.PlayPayloadHandler<T> wrapClientHandler(IConnectionToServer connection, BiConsumer<T, ClientPacketContext> consumer) {
		return (t, payloadContext) -> {
			var clientPacketContext = new ClientPacketContext(payloadContext.player(), connection);
			consumer.accept(t, clientPacketContext);
		};
	}
}
