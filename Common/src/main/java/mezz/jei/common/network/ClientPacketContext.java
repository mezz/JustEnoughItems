package mezz.jei.common.network;

import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.player.LocalPlayer;

public record ClientPacketContext(LocalPlayer player,
                                  IConnectionToServer connection,
                                  IServerConfig serverConfig,
                                  IWorldConfig worldConfig
) {
}
