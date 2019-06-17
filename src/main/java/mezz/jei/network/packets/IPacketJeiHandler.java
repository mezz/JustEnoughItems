package mezz.jei.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public interface IPacketJeiHandler {
	void readPacketData(PacketBuffer buf, PlayerEntity player);
}
