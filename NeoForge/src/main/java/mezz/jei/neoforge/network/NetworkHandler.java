package mezz.jei.neoforge.network;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.*;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

import java.util.Locale;

public class NetworkHandler {
	private static final String TO_CLIENT_NAMESPACE = ModIds.JEI_ID + "_to_client";
	private static final String TO_SERVER_NAMESPACE = ModIds.JEI_ID + "_to_server";

	private final ResourceLocation channelId;
	private final String protocolVersion;
	private final IServerConfig serverConfig;

	public NetworkHandler(ResourceLocation channelId, String protocolVersion, IServerConfig serverConfig) {
		this.channelId = channelId;
		this.protocolVersion = protocolVersion;
		this.serverConfig = serverConfig;
	}

	public ResourceLocation getChannelId() {
		return channelId;
	}

	public void registerServerPacketHandler(
			ServerPacketRouter packetRouter, IConnectionToClient connection, PermanentEventSubscriptions subscriptions
	) {
		subscriptions.register(RegisterPayloadHandlerEvent.class, ev -> {
			var registrar = ev.registrar(TO_SERVER_NAMESPACE)
					.versioned(this.protocolVersion)
					.optional();
			// TODO use configuration phase?
			packetRouter.handlers.forEach((key, value) -> {
				ResourceLocation name = toServerID(key);
				registrar.play(
						name,
						buf -> new WrappingPayload<>(value.readPacketData(buf), name),
						(packet, context) -> WrappingPayload.processToServer(packet, context, connection, serverConfig)
				);
			});
		});
	}

	@OnlyIn(Dist.CLIENT)
	public void registerClientPacketHandler(
			ClientPacketRouter packetRouter, IConnectionToServer connection, PermanentEventSubscriptions subscriptions
	) {
		subscriptions.register(RegisterPayloadHandlerEvent.class, ev -> {
			var registrar = ev.registrar(TO_CLIENT_NAMESPACE)
					.versioned(this.protocolVersion)
					.optional();
			// TODO use configuration phase?
			packetRouter.clientHandlers.forEach((key, value) -> {
				ResourceLocation name = toClientID(key);
				registrar.play(
						name,
						buf -> new WrappingPayload<>(value.readPacketData(buf), name),
						(packet, context) -> WrappingPayload.processToClient(packet, context, connection, serverConfig)
				);
			});
		});
	}

	public static ResourceLocation toServerID(PacketIdServer id) {
		return new ResourceLocation(TO_SERVER_NAMESPACE, id.name().toLowerCase(Locale.ROOT));
	}

	public static ResourceLocation toClientID(PacketIdClient id) {
		return new ResourceLocation(TO_CLIENT_NAMESPACE, id.name().toLowerCase(Locale.ROOT));
	}
}
