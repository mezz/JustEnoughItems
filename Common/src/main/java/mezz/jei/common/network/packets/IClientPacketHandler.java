package mezz.jei.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IClientPacketHandler {
	@Nullable
	PacketJeiToClient readPacketData(FriendlyByteBuf buffer);
}
