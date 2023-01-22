package mezz.jei.common.network;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.config.IWorldConfig;
import net.minecraft.client.player.LocalPlayer;

public record ClientPacketContext(LocalPlayer player,
                                  IConnectionToServer connection,
                                  IServerConfig serverConfig,
                                  IWorldConfig worldConfig
) {
}
