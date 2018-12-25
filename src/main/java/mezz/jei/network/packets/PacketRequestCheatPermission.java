package mezz.jei.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import mezz.jei.network.IPacketId;
import mezz.jei.network.Network;
import mezz.jei.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;

public class PacketRequestCheatPermission extends PacketJei {
	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.CHEAT_PERMISSION_REQUEST;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		// the packet itself is the only data needed
	}

	public static void readPacketData(PacketBuffer buf, EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP sender = (EntityPlayerMP) player;
			boolean hasPermission = CommandUtilServer.hasPermission(sender);
			PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

			Network.sendPacketToClient(packetCheatPermission, sender);
		}
	}
}
