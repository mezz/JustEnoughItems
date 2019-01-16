package mezz.jei.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;

public class PacketDeletePlayerItem extends PacketJei {
	private final ItemStack itemStack;

	public PacketDeletePlayerItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.DELETE_ITEM;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		int itemId = Item.getIdFromItem(itemStack.getItem());
		buf.writeShort(itemId);
	}

	public static void readPacketData(PacketBuffer buf, EntityPlayer player) {
		int itemId = buf.readShort();
		Item item = Item.getItemById(itemId);
		ItemStack playerItem = player.inventory.getItemStack();
		if (!playerItem.isEmpty() && playerItem.getItem() == item) {
			player.inventory.setItemStack(ItemStack.EMPTY);
		}
	}
}
