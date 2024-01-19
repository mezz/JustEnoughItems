package mezz.jei.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IServerPacketHandler {
	@Nullable
	PacketJeiToServer readPacketData(FriendlyByteBuf buffer);
}
