package mezz.jei.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.config.IServerConfig;
import mezz.jei.config.ServerConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdClient;
import mezz.jei.util.CommandUtilServer;

import java.util.ArrayList;
import java.util.List;

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
		if (!hasPermission) {
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.1", TextFormatting.RED);

			IServerConfig serverConfig = ServerConfig.getInstance();
			List<String> allowedCheatingMethods = new ArrayList<>();
			if (serverConfig.isCheatModeEnabledForOp()) {
				allowedCheatingMethods.add("jei.chat.error.no.cheat.permission.op");
			}
			if (serverConfig.isCheatModeEnabledForCreative()) {
				allowedCheatingMethods.add("jei.chat.error.no.cheat.permission.creative");
			}
			if (serverConfig.isCheatModeEnabledForGive()) {
				allowedCheatingMethods.add("jei.chat.error.no.cheat.permission.give");
			}

			if (allowedCheatingMethods.isEmpty()) {
				CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.disabled", TextFormatting.RED);
			} else {
				CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.enabled", TextFormatting.RED);
				for (String allowedCheatingMethod : allowedCheatingMethods) {
					CommandUtilServer.writeChatMessage(player, allowedCheatingMethod, TextFormatting.RED);
				}
			}

			worldConfig.setCheatItemsEnabled(false);
			player.closeContainer();
		}
	}
}
