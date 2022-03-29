package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketData;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.util.ChatUtilServer;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

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

	public static void readPacketData(ClientPacketData data) {
		FriendlyByteBuf buf = data.buf();
		boolean hasPermission = buf.readBoolean();
		if (!hasPermission) {
			LocalPlayer player = data.player();
			ChatUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.1", ChatFormatting.RED);

			IServerConfig serverConfig = data.serverConfig();
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
				ChatUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.disabled", ChatFormatting.RED);
			} else {
				ChatUtilServer.writeChatMessage(player, "jei.chat.error.no.cheat.permission.enabled", ChatFormatting.RED);
				for (String allowedCheatingMethod : allowedCheatingMethods) {
					ChatUtilServer.writeChatMessage(player, allowedCheatingMethod, ChatFormatting.RED);
				}
			}

			IWorldConfig worldConfig = data.worldConfig();
			worldConfig.setCheatItemsEnabled(false);
			player.closeContainer();
		}
	}
}
