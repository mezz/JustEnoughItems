package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.network.packets.handlers.ClientCheatPermissionHandler;
import net.minecraft.network.FriendlyByteBuf;


public class PacketCheatPermission extends PacketJeiToClient {
	private final boolean hasPermission;

	public PacketCheatPermission(boolean hasPermission) {
		this.hasPermission = hasPermission;
	}

	@Override
	public PacketIdClient getPacketId() {
		return PacketIdClient.CHEAT_PERMISSION;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeBoolean(hasPermission);
	}

	@Override
	public void processOnClientThread(ClientPacketContext context) {
		ClientCheatPermissionHandler.handleHasCheatPermission(context, hasPermission);
	}

	public static PacketCheatPermission readPacketData(FriendlyByteBuf buf) {
		boolean hasPermission = buf.readBoolean();
		return new PacketCheatPermission(hasPermission);
	}
}
