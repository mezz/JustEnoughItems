package mezz.jei.network.packets;

import mezz.jei.config.IServerConfig;
import mezz.jei.config.ServerConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.ChatFormatting;

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
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeBoolean(hasPermission);
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player, IWorldConfig worldConfig) {
		boolean hasPermission = buf.readBoolean();
		if (!hasPermission) {
			CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.1", ChatFormatting.RED);

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
				CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.disabled", ChatFormatting.RED);
			} else {
				CommandUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.enabled", ChatFormatting.RED);
				for (String allowedCheatingMethod : allowedCheatingMethods) {
					CommandUtilServer.writeChatMessage(player, allowedCheatingMethod, ChatFormatting.RED);
				}
			}

			worldConfig.setCheatItemsEnabled(false);
			player.closeContainer();
		}
	}
}
