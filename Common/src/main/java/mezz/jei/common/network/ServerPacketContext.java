package mezz.jei.common.network;

import mezz.jei.core.config.IServerConfig;
import net.minecraft.server.level.ServerPlayer;

public record ServerPacketContext(ServerPlayer player,
                                  IServerConfig serverConfig,
                                  IConnectionToClient connection
) {
}
