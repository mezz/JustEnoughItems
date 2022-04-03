package mezz.jei.common.network;

import net.minecraft.network.FriendlyByteBuf;

public record ServerPacketData(FriendlyByteBuf buf, ServerPacketContext context) {
}
