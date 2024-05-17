package mezz.jei.common.network;

import net.minecraft.client.player.LocalPlayer;

public record ClientPacketContext(LocalPlayer player, IConnectionToServer connection) {
}
