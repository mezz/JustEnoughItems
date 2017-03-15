package mezz.jei.network.packets;

import java.io.IOException;

import mezz.jei.JustEnoughItems;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class PacketGiveItemStack extends PacketJei {
	private final ItemStack itemStack;

	public PacketGiveItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.GIVE_BIG;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		NBTTagCompound nbt = itemStack.serializeNBT();
		buf.writeCompoundTag(nbt);
	}

	public static class Handler implements IPacketJeiHandler {
		@Override
		public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP sender = (EntityPlayerMP) player;

				NBTTagCompound itemStackSerialized = buf.readCompoundTag();
				if (itemStackSerialized != null) {
					ItemStack itemStack = new ItemStack(itemStackSerialized);
					if (!itemStack.isEmpty()) {
						if (CommandUtilServer.hasPermission(sender, itemStack)) {
							CommandUtilServer.executeGive(sender, itemStack);
						} else {
							JustEnoughItems.getProxy().sendPacketToClient(new PacketCheatPermission(false), sender);
						}
					}
				}
			}
		}

	}
}
