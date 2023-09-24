package mezz.jei.forge.network;

import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.ServerPacketRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ResourceLocation channelId;
	private final EventNetworkChannel channel;

	public NetworkHandler(ResourceLocation channelId, int protocolVersion) {
		this.channelId = channelId;
		this.channel = ChannelBuilder.named(channelId)
			.networkProtocolVersion(protocolVersion)
			.optionalClient()
			.optionalServer()
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
			CustomPayloadEvent.Context context = event.getSource();
			if (context.isServerSide()) {
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
			CustomPayloadEvent.Context context = event.getSource();
			if (context.isClientSide()) {
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
