package mezz.jei.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

	public static void readPacketData(PacketBuffer buf, PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			ServerPlayerEntity sender = (ServerPlayerEntity) player;
			boolean hasPermission = CommandUtilServer.hasPermission(sender);
			PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

			Network.sendPacketToClient(packetCheatPermission, sender);
		}
	}
}
