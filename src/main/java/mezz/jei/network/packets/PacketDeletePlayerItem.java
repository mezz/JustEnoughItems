package mezz.jei.network.packets;

import java.io.IOException;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

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
		buf.writeItemStack(itemStack);
	}

	public static class Handler implements IPacketJeiHandler {
		@Override
		public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
			ItemStack itemStack = buf.readItemStack();
			ItemStack playerItem = player.inventory.getItemStack();
			if (ItemStack.areItemStacksEqual(itemStack, playerItem)) {
				player.inventory.setItemStack(ItemStack.EMPTY);
			}
		}
	}
}
