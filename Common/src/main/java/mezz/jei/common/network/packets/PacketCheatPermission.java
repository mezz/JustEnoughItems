package mezz.jei.common.network.packets;

import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.ClientPacketData;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.network.packets.handlers.ClientCheatPermissionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

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

	public static CompletableFuture<Void> readPacketData(ClientPacketData data) {
		FriendlyByteBuf buf = data.buf();
		boolean hasPermission = buf.readBoolean();
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketContext context = data.context();
		return minecraft.submit(() -> ClientCheatPermissionHandler.handleHasCheatPermission(context, hasPermission));
	}
}
