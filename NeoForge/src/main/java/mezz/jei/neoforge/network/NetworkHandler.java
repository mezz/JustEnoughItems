package mezz.jei.neoforge.network;

import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.ServerPacketRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.event.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ResourceLocation channelId;
	private final EventNetworkChannel channel;

	public NetworkHandler(ResourceLocation channelId, String protocolVersion) {
		this.channelId = channelId;
		this.channel = NetworkRegistry.ChannelBuilder.named(channelId)
			.networkProtocolVersion(() -> protocolVersion)
			.clientAcceptedVersions(s -> NetworkRegistry.ABSENT.version().equals(s) || s.equals(protocolVersion))
			.serverAcceptedVersions(s -> NetworkRegistry.ABSENT.version().equals(s) || s.equals(protocolVersion))
			.eventNetworkChannel();
	}

	public ResourceLocation getChannelId() {
		return channelId;
	}

	public EventNetworkChannel getChannel() {
		return channel;
	}

	public void registerServerPacketHandler(ServerPacketRouter packetRouter) {
		channel.addListener(event -> {
			NetworkEvent.Context context = event.getSource();
			if (context.getDirection() == PlayNetworkDirection.PLAY_TO_SERVER) {
				ServerPlayer sender = context.getSender();
				if (sender == null) {
					LOGGER.error("Packet error, the sender player is missing for event: {}", event);
					return;
				}
				packetRouter.onPacket(event.getPayload(), sender);
				context.setPacketHandled(true);
			}
		});
	}

	@OnlyIn(Dist.CLIENT)
	public void registerClientPacketHandler(ClientPacketRouter packetRouter) {
		channel.addListener(event -> {
			NetworkEvent.Context context = event.getSource();
			if (context.getDirection() == PlayNetworkDirection.PLAY_TO_CLIENT) {
				Minecraft minecraft = Minecraft.getInstance();
				LocalPlayer player = minecraft.player;
				if (player == null) {
					LOGGER.error("Packet error, the local player is missing for event: {}", event);
					return;
				}
				packetRouter.onPacket(event.getPayload(), player);
				context.setPacketHandled(true);
			}
		});
	}
}
