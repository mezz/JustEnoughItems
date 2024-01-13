package mezz.jei.neoforge.network;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.network.packets.PacketJeiToClient;
import mezz.jei.common.network.packets.PacketJeiToServer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import javax.annotation.Nullable;

public record WrappingPayload<T extends PacketJei<?>>(
		@Nullable T data, ResourceLocation id
) implements CustomPacketPayload {

	@Override
	public void write(FriendlyByteBuf fbb) {
		if (data != null) {
			data.writePacketData(fbb);
		}
	}

	public static void processToServer(
			WrappingPayload<PacketJeiToServer> packet,
			PlayPayloadContext context,
			IConnectionToClient connection,
			IServerConfig serverConfig
	) {
		if (packet.data != null) {
			ServerPacketContext jeiContext = new ServerPacketContext(
					(ServerPlayer) context.player().orElseThrow(),
					serverConfig,
					connection
			);
			context.workHandler().execute(() -> packet.data.processOnServerThread(jeiContext));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void processToClient(
			WrappingPayload<PacketJeiToClient> packet,
			PlayPayloadContext context,
			IConnectionToServer connection,
			IServerConfig serverConfig
	) {
		if (packet.data != null) {
			ClientPacketContext jeiContext = new ClientPacketContext(
					(LocalPlayer) context.player().orElseThrow(),
					connection,
					serverConfig
			);
			context.workHandler().execute(() -> packet.data.processOnClientThread(jeiContext));
		}
	}
}
