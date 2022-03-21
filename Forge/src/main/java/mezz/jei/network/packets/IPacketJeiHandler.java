package mezz.jei.network.packets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

public interface IPacketJeiHandler {
	void readPacketData(FriendlyByteBuf buf, Player player);
}
