package mezz.jei.common.network.packets;

import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.config.GiveMode;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class PacketGiveItemStack extends PacketJeiToServer {
	private final ItemStack itemStack;
	private final GiveMode giveMode;

	public PacketGiveItemStack(ItemStack itemStack, GiveMode giveMode) {
		this.itemStack = itemStack;
		this.giveMode = giveMode;
	}

	@Override
	public PacketIdServer getPacketId() {
		return PacketIdServer.GIVE_ITEM;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeItem(itemStack);
		buf.writeEnum(giveMode);
	}

	public static PacketGiveItemStack readPacketData(FriendlyByteBuf buf) {
		ItemStack itemStack = buf.readItem();
		GiveMode giveMode = buf.readEnum(GiveMode.class);
		return new PacketGiveItemStack(itemStack, giveMode);
	}

	@Override
	public void processOnServerThread(ServerPacketContext context) {
		ServerCommandUtil.executeGive(context, itemStack, giveMode);
	}
}
