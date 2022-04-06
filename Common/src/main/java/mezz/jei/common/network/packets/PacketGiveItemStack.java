package mezz.jei.common.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.core.config.GiveMode;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class PacketGiveItemStack extends PacketJei {
	private final ItemStack itemStack;
	private final GiveMode giveMode;

	public PacketGiveItemStack(ItemStack itemStack, GiveMode giveMode) {
		this.itemStack = itemStack;
		this.giveMode = giveMode;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.GIVE_ITEM;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeItem(itemStack);
		buf.writeEnum(giveMode);
	}

	public static void readPacketData(ServerPacketData data) {
		FriendlyByteBuf buf = data.buf();
		ItemStack itemStack = buf.readItem();
		if (!itemStack.isEmpty()) {
			GiveMode giveMode = buf.readEnum(GiveMode.class);
			ServerPacketContext context = data.context();
			ServerCommandUtil.executeGive(context, itemStack, giveMode);
		}
	}
}
