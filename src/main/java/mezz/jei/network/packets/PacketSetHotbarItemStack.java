package mezz.jei.network.packets;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

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

	public static void readPacketData(FriendlyByteBuf buf, Player player) {
		if (player instanceof ServerPlayer sender) {

			ItemStack itemStack = buf.readItem();
			if (!itemStack.isEmpty()) {
				int hotbarSlot = buf.readVarInt();
				CommandUtilServer.setHotbarSlot(sender, itemStack, hotbarSlot);
			}
		}
	}
}
