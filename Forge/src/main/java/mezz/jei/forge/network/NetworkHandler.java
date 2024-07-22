package mezz.jei.forge.network;

import mezz.jei.common.Internal;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;

public class NetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IServerConfig serverConfig;
	private final IConnectionToServer connectionToServer;
	private final IConnectionToClient connectionToClient;
	private final Channel<CustomPacketPayload> channel;

	public NetworkHandler(IServerConfig serverConfig, ResourceLocation channelId, int protocolVersion) {
		this.serverConfig = serverConfig;

		this.connectionToServer = new ConnectionToServer(this);
		Internal.setServerConnection(this.connectionToServer);
		this.connectionToClient = new ConnectionToClient(this);

		this.channel = ChannelBuilder.named(channelId)
			.networkProtocolVersion(protocolVersion)
			.optional()
			.payloadChannel()
			.play()
			.serverbound()
				.add(PacketDeletePlayerItem.TYPE, PacketDeletePlayerItem.STREAM_CODEC, wrapServerHandler(PacketDeletePlayerItem::process))
				.add(PacketGiveItemStack.TYPE, PacketGiveItemStack.STREAM_CODEC, wrapServerHandler(PacketGiveItemStack::process))
				.add(PacketRecipeTransfer.TYPE, PacketRecipeTransfer.STREAM_CODEC, wrapServerHandler(PacketRecipeTransfer::process))
				.add(PacketSetHotbarItemStack.TYPE, PacketSetHotbarItemStack.STREAM_CODEC, wrapServerHandler(PacketSetHotbarItemStack::process))
				.add(PacketRequestCheatPermission.TYPE, PacketRequestCheatPermission.STREAM_CODEC, wrapServerHandler(PacketRequestCheatPermission::process))
			.clientbound()
				.add(PacketCheatPermission.TYPE, PacketCheatPermission.STREAM_CODEC, wrapClientHandler(PacketCheatPermission::process))
			.build();
	}

	public IConnectionToServer getConnectionToServer() {
		return connectionToServer;
	}

	public Channel<CustomPacketPayload> getChannel() {
		return channel;
	}

	private <T extends PlayToClientPacket<T>> BiConsumer<T, CustomPayloadEvent.Context> wrapClientHandler(BiConsumer<T, ClientPacketContext> consumer) {
		return (t, payloadContext) -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				var clientPacketContext = new ClientPacketContext(player, connectionToServer);
				payloadContext.setPacketHandled(true);
				payloadContext.enqueueWork(() -> {
					consumer.accept(t, clientPacketContext);
				});
			} else {
				LOGGER.debug("Tried to handle packet payload with no player: {}", t.type());
			}
		};
	}

	private <T extends PlayToServerPacket<T>> BiConsumer<T, CustomPayloadEvent.Context> wrapServerHandler(BiConsumer<T, ServerPacketContext> consumer) {
		return (t, payloadContext) -> {
			ServerPlayer player = payloadContext.getSender();
			if (player != null) {
				var serverPacketContext = new ServerPacketContext(player, serverConfig, connectionToClient);
				payloadContext.setPacketHandled(true);
				payloadContext.enqueueWork(() -> {
					consumer.accept(t, serverPacketContext);
				});
			} else {
				LOGGER.debug("Tried to handle packet payload with no player: {}", t.type());
			}
		};
	}
}
