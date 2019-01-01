package mezz.jei.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public interface IPacketJeiHandler {
	void readPacketData(PacketBuffer buf, EntityPlayer player);
}
