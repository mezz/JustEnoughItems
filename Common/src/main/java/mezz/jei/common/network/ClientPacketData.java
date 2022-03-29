package mezz.jei.common.network;

import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

public record ClientPacketData(FriendlyByteBuf buf,
							   LocalPlayer player,
							   IConnectionToServer connection,
							   IServerConfig serverConfig,
							   IWorldConfig worldConfig
) {
}
