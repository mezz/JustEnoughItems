package mezz.jei.network.packets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.ChatFormatting;

import mezz.jei.config.IWorldConfig;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdClient;
import mezz.jei.util.CommandUtilServer;

public class PacketCheatPermission extends PacketJei {
	private final boolean hasPermission;

	public PacketCheatPermission(boolean hasPermission) {
		this.hasPermission = hasPermission;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdClient.CHEAT_PERMISSION;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeBoolean(hasPermission);
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player, IWorldConfig worldConfig) {
		boolean hasPermission = buf.readBoolean();
		if (!hasPermission && worldConfig.isCheatItemsEnabled()) {
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.1", ChatFormatting.RED);
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.2", ChatFormatting.RED);
			worldConfig.setCheatItemsEnabled(false);
			player.closeContainer();
		}
	}
}
