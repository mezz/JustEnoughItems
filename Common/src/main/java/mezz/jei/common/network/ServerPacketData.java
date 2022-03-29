package mezz.jei.common.network;

import mezz.jei.core.config.IServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ServerPacketData(FriendlyByteBuf buf,
							   ServerPlayer player,
							   IServerConfig serverConfig,
							   IConnectionToClient connection
) {
}
