package mezz.jei.neoforge.network;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PacketCheatPermission;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PacketGiveItemStack;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;
import mezz.jei.common.network.packets.PacketSetHotbarItemStack;
import mezz.jei.common.network.packets.PlayToClientPacket;
import mezz.jei.common.network.packets.PlayToServerPacket;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;

import java.util.function.BiConsumer;

public class NetworkHandler {
	private static final String TO_CLIENT_NAMESPACE = ModIds.JEI_ID + "_to_client";
	private static final String TO_SERVER_NAMESPACE = ModIds.JEI_ID + "_to_server";

	private final String protocolVersion;
	private final IServerConfig serverConfig;

	public NetworkHandler(String protocolVersion, IServerConfig serverConfig) {
		this.protocolVersion = protocolVersion;
		this.serverConfig = serverConfig;
	}

	public void registerServerPacketHandler(IConnectionToClient connection, PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RegisterPayloadHandlersEvent.class, ev -> {
			var registrar = ev.registrar(TO_CLIENT_NAMESPACE)
				.executesOn(HandlerThread.MAIN)
				.versioned(this.protocolVersion)
				.optional();

			registrar.playToServer(PacketDeletePlayerItem.TYPE, PacketDeletePlayerItem.STREAM_CODEC, wrapServerHandler(connection, PacketDeletePlayerItem::process));
			registrar.playToServer(PacketGiveItemStack.TYPE, PacketGiveItemStack.STREAM_CODEC, wrapServerHandler(connection, PacketGiveItemStack::process));
			registrar.playToServer(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, wrapServerHandler(connection, PacketRecipeTransfer::process));
			registrar.playToServer(PacketSetHotbarItemStack.TYPE, PacketSetHotbarItemStack.STREAM_CODEC, wrapServerHandler(connection, PacketSetHotbarItemStack::process));
			registrar.playToServer(PacketRequestCheatPermission.TYPE, PacketRequestCheatPermission.STREAM_CODEC, wrapServerHandler(connection, PacketRequestCheatPermission::process));
		});
	}

	@OnlyIn(Dist.CLIENT)
	public void registerClientPacketHandler(IConnectionToServer connection, PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RegisterPayloadHandlersEvent.class, ev -> {
			var registrar = ev.registrar(TO_SERVER_NAMESPACE)
				.executesOn(HandlerThread.MAIN)
				.versioned(this.protocolVersion)
				.optional();

			registrar.playToClient(PacketCheatPermission.TYPE, PacketCheatPermission.STREAM_CODEC, wrapClientHandler(connection, PacketCheatPermission::process));
		});
	}

	private <T extends PlayToClientPacket<T>> IPayloadHandler<T> wrapClientHandler(IConnectionToServer connection, BiConsumer<T, ClientPacketContext> consumer) {
		return (t, payloadContext) -> {
			LocalPlayer player = (LocalPlayer) payloadContext.player();
			var clientPacketContext = new ClientPacketContext(player, connection);
			consumer.accept(t, clientPacketContext);
		};
	}

	private <T extends PlayToServerPacket<T>> IPayloadHandler<T> wrapServerHandler(IConnectionToClient connection, BiConsumer<T, ServerPacketContext> consumer) {
		return (t, payloadContext) -> {
			ServerPlayer player = (ServerPlayer) payloadContext.player();
			var serverPacketContext = new ServerPacketContext(player, serverConfig, connection);
			consumer.accept(t, serverPacketContext);
		};
	}
}
