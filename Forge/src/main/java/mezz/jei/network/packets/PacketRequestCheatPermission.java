package mezz.jei.network.packets;

import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

import mezz.jei.common.network.IPacketId;
import mezz.jei.network.Network;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;

public class PacketRequestCheatPermission extends PacketJei {
	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.CHEAT_PERMISSION_REQUEST;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		// the packet itself is the only data needed
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player) {
		if (player instanceof ServerPlayer sender) {
			boolean hasPermission = CommandUtilServer.hasPermissionForCheatMode(sender);
			PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

			Network.sendPacketToClient(packetCheatPermission, sender);
		}
	}
}
