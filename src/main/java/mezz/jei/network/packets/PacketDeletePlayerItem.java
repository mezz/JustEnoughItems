package mezz.jei.network.packets;

import java.io.IOException;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class PacketDeletePlayerItem extends PacketJEI {
	private ItemStack itemStack;

	public PacketDeletePlayerItem() {

	}

	public PacketDeletePlayerItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.DELETE_ITEM;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeItemStackToBuffer(itemStack);
	}

	@Override
	public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		itemStack = buf.readItemStackFromBuffer();
		ItemStack playerItem = player.inventory.getItemStack();
		if (ItemStack.areItemStacksEqual(itemStack, playerItem)) {
			player.inventory.setItemStack(null);
		}
	}
}
