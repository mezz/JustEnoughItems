package mezz.jei.network.packets;

import javax.annotation.Nonnull;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import mezz.jei.Internal;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;

public class PacketDeletePlayerItem extends PacketJEI {
	private ItemStack itemStack;

	public PacketDeletePlayerItem() {

	}

	public PacketDeletePlayerItem(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.DELETE_ITEM;
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeItemStackToBuffer(itemStack);
	}

	@Override
	public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		itemStack = buf.readItemStackFromBuffer();
		ItemStack playerItem = player.inventory.getItemStack();
		if (Internal.getStackHelper().isIdentical(itemStack, playerItem)) {
			player.inventory.setItemStack(null);
		}
	}
}
