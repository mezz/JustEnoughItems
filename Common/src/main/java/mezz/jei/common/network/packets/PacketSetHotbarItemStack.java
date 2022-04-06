package mezz.jei.common.network.packets;

import com.google.common.base.Preconditions;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.util.ServerCommandUtil;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PacketSetHotbarItemStack extends PacketJei {
	private final ItemStack itemStack;
	private final int hotbarSlot;

	public PacketSetHotbarItemStack(ItemStack itemStack, int hotbarSlot) {
		ErrorUtil.checkNotNull(itemStack, "itemStack");
		Preconditions.checkArgument(Inventory.isHotbarSlot(hotbarSlot), "hotbar slot must be in the hotbar. got: " + hotbarSlot);
		this.itemStack = itemStack;
		this.hotbarSlot = hotbarSlot;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.SET_HOTBAR_ITEM;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeItem(itemStack);
		buf.writeVarInt(hotbarSlot);
	}

	public static void readPacketData(ServerPacketData data) {
		FriendlyByteBuf buf = data.buf();
		ItemStack itemStack = buf.readItem();
		if (!itemStack.isEmpty()) {
			int hotbarSlot = buf.readVarInt();
			ServerPacketContext context = data.context();
			ServerCommandUtil.setHotbarSlot(context, itemStack, hotbarSlot);
		}
	}
}
