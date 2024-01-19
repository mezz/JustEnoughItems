package mezz.jei.common.network.packets;

import com.google.common.base.Preconditions;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.util.ServerCommandUtil;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PacketSetHotbarItemStack extends PacketJeiToServer {
	private final ItemStack itemStack;
	private final int hotbarSlot;

	public PacketSetHotbarItemStack(ItemStack itemStack, int hotbarSlot) {
		ErrorUtil.checkNotNull(itemStack, "itemStack");
		Preconditions.checkArgument(Inventory.isHotbarSlot(hotbarSlot), "hotbar slot must be in the hotbar. got: " + hotbarSlot);
		this.itemStack = itemStack;
		this.hotbarSlot = hotbarSlot;
	}

	@Override
	public PacketIdServer getPacketId() {
		return PacketIdServer.SET_HOTBAR_ITEM;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeItem(itemStack);
		buf.writeVarInt(hotbarSlot);
	}

	@Override
	public void processOnServerThread(ServerPacketContext context) {
		ServerCommandUtil.setHotbarSlot(context, itemStack, hotbarSlot);
	}

	public static PacketSetHotbarItemStack readPacketData(FriendlyByteBuf buf) {
		ItemStack itemStack = buf.readItem();
		int hotbarSlot = buf.readVarInt();
		return new PacketSetHotbarItemStack(itemStack, hotbarSlot);
	}
}
