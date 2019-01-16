package mezz.jei.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.config.Config;
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
	public void writePacketData(PacketBuffer buf) {
		buf.writeBoolean(hasPermission);
	}

	public static void readPacketData(PacketBuffer buf, EntityPlayer player) {
		boolean hasPermission = buf.readBoolean();
		if (!hasPermission && Config.isCheatItemsEnabled()) {
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.1", TextFormatting.RED);
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.2", TextFormatting.RED);
			Config.setCheatItemsEnabled(false);
			player.closeScreen();
		}
	}
}
