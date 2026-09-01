package mezz.jei.forge.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.ClientPacketRouter;
import mezz.jei.common.network.ServerPacketRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ResourceLocation channelId;
	private final EventNetworkChannel channel;
	private final EventNetworkChannel recipeTransferResultChannel;

	public NetworkHandler(ResourceLocation channelId, String protocolVersion) {
		this.channelId = channelId;
		this.channel = createChannel(channelId, protocolVersion);
		this.recipeTransferResultChannel = createChannel(Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID, protocolVersion);
	}

	private static EventNetworkChannel createChannel(ResourceLocation channelId, String protocolVersion) {
		return NetworkRegistry.newEventChannel(
			channelId,
			() -> protocolVersion,
			NetworkHandler::isClientAcceptedVersion,
			NetworkHandler::isServerAcceptedVersion
		);
	}

	public ResourceLocation getChannelId() {
		return channelId;
	}

	public ResourceLocation getRecipeTransferResultChannelId() {
		return Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID;
	}

	private static boolean isClientAcceptedVersion(String version) {
		return true;
	}

	private static boolean isServerAcceptedVersion(String version) {
		return true;
	}

	public void registerServerPacketHandler(ServerPacketRouter packetRouter) {
		registerServerPacketHandler(channel, packetRouter);
		registerServerPacketHandler(recipeTransferResultChannel, packetRouter);
	}

	private static void registerServerPacketHandler(EventNetworkChannel channel, ServerPacketRouter packetRouter) {
		channel.addListener((NetworkEvent.ClientCustomPayloadEvent event) -> {
			NetworkEvent.Context context = event.getSource().get();
			ServerPlayer player = context.getSender();
			if (player == null) {
				LOGGER.error("Packet error, the sender player is missing for event: {}", event);
				return;
			}
			context.setPacketHandled(true);
			packetRouter.onPacket(event.getPayload(), player);
		});
	}

	@OnlyIn(Dist.CLIENT)
	public void registerClientPacketHandler(ClientPacketRouter packetRouter) {
		registerClientPacketHandler(channel, packetRouter);
		registerClientPacketHandler(recipeTransferResultChannel, packetRouter);
	}

	@OnlyIn(Dist.CLIENT)
	private static void registerClientPacketHandler(EventNetworkChannel channel, ClientPacketRouter packetRouter) {
		channel.addListener((NetworkEvent.ServerCustomPayloadEvent event) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player == null) {
				LOGGER.error("Packet error, the local player is missing for event: {}", event);
				return;
			}
			NetworkEvent.Context context = event.getSource().get();
			context.setPacketHandled(true);
			packetRouter.onPacket(event.getPayload(), player);
		});
	}
}
