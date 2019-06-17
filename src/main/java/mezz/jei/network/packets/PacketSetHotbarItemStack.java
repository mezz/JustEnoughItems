package mezz.jei.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
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
		Preconditions.checkArgument(PlayerInventory.isHotbar(hotbarSlot), "hotbar slot must be in the hotbar. got: " + hotbarSlot);
		this.itemStack = itemStack;
		this.hotbarSlot = hotbarSlot;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.SET_HOTBAR_ITEM;
	}

	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeItemStack(itemStack);
		buf.writeVarInt(hotbarSlot);
	}

	public static void readPacketData(PacketBuffer buf, PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			ServerPlayerEntity sender = (ServerPlayerEntity) player;

			ItemStack itemStack = buf.readItemStack();
			if (!itemStack.isEmpty()) {
				int hotbarSlot = buf.readVarInt();
				CommandUtilServer.setHotbarSlot(sender, itemStack, hotbarSlot);
			}
		}
	}
}
