package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketData;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.network.packets.handlers.ClientCheatPermissionHandler;
import net.minecraft.network.FriendlyByteBuf;

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
		ClientCheatPermissionHandler.handleHasCheatPermission(data.context(), hasPermission);
	}
}
