package mezz.jei.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

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
	public void writePacketData(PacketBuffer buf) {
		buf.writeBoolean(hasPermission);
	}

	public static void readPacketData(PacketBuffer buf, PlayerEntity player, IWorldConfig worldConfig) {
		boolean hasPermission = buf.readBoolean();
		if (!hasPermission && worldConfig.isCheatItemsEnabled()) {
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.1", TextFormatting.RED);
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.2", TextFormatting.RED);
			worldConfig.setCheatItemsEnabled(false);
			player.closeScreen();
		}
	}
}
