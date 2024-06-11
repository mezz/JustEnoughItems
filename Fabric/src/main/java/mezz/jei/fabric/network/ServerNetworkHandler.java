package mezz.jei.fabric.network;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PacketGiveItemStack;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;
import mezz.jei.common.network.packets.PacketSetHotbarItemStack;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public final class ServerNetworkHandler {
	private ServerNetworkHandler() {}

	public static void registerServerPacketHandlers(IConnectionToClient connection, IServerConfig serverConfig) {
		PayloadTypeRegistry.playS2C().register(PacketDeletePlayerItem.TYPE, PacketDeletePlayerItem.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(PacketGiveItemStack.TYPE, PacketGiveItemStack.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(PacketSetHotbarItemStack.TYPE, PacketSetHotbarItemStack.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(PacketRequestCheatPermission.TYPE, PacketRequestCheatPermission.STREAM_CODEC);

		PayloadTypeRegistry.playC2S().register(PacketDeletePlayerItem.TYPE, PacketDeletePlayerItem.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(PacketGiveItemStack.TYPE, PacketGiveItemStack.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(PacketSetHotbarItemStack.TYPE, PacketSetHotbarItemStack.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(PacketRequestCheatPermission.TYPE, PacketRequestCheatPermission.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(PacketDeletePlayerItem.TYPE, wrapServerHandler(connection, serverConfig, PacketDeletePlayerItem::process));
		ServerPlayNetworking.registerGlobalReceiver(PacketGiveItemStack.TYPE, wrapServerHandler(connection, serverConfig, PacketGiveItemStack::process));
		ServerPlayNetworking.registerGlobalReceiver(PacketRecipeTransfer.TYPE, wrapServerHandler(connection, serverConfig, PacketRecipeTransfer::process));
		ServerPlayNetworking.registerGlobalReceiver(PacketSetHotbarItemStack.TYPE, wrapServerHandler(connection, serverConfig, PacketSetHotbarItemStack::process));
		ServerPlayNetworking.registerGlobalReceiver(PacketRequestCheatPermission.TYPE, wrapServerHandler(connection, serverConfig, PacketRequestCheatPermission::process));
	}

	private static <T extends PlayToServerPacket<T>> ServerPlayNetworking.PlayPayloadHandler<T> wrapServerHandler(
		IConnectionToClient connection,
		IServerConfig serverConfig,
		BiConsumer<T, ServerPacketContext> consumer
	) {
		return (t, payloadContext) -> {
			ServerPlayer player = payloadContext.player();
			var serverPacketContext = new ServerPacketContext(player, serverConfig, connection);
			consumer.accept(t, serverPacketContext);
		};
	}
}
