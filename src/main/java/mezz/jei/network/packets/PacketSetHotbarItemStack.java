package mezz.jei.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import com.google.common.base.Preconditions;
import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;
import mezz.jei.util.ErrorUtil;

public class PacketSetHotbarItemStack extends PacketJei {
	private final ItemStack itemStack;
	private final int hotbarSlot;

	public PacketSetHotbarItemStack(ItemStack itemStack, int hotbarSlot) {
		ErrorUtil.checkNotNull(itemStack, "itemStack");
		Preconditions.checkArgument(InventoryPlayer.isHotbar(hotbarSlot), "hotbar slot must be in the hotbar. got: " + hotbarSlot);
		this.itemStack = itemStack;
		this.hotbarSlot = hotbarSlot;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.SET_HOTBAR_ITEM;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		NBTTagCompound nbt = itemStack.serializeNBT();
		buf.writeCompoundTag(nbt);
		buf.writeVarInt(hotbarSlot);
	}

	public static void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP sender = (EntityPlayerMP) player;

			NBTTagCompound itemStackSerialized = buf.readCompoundTag();
			if (itemStackSerialized != null) {
				int hotbarSlot = buf.readVarInt();
				ItemStack itemStack = new ItemStack(itemStackSerialized);
				if (!itemStack.isEmpty()) {
					CommandUtilServer.setHotbarSlot(sender, itemStack, hotbarSlot);
				}
			}
		}
	}
}
