package mezz.jei.common.network;

import net.minecraft.network.FriendlyByteBuf;

public record ClientPacketData(FriendlyByteBuf buf, ClientPacketContext context) {
}
