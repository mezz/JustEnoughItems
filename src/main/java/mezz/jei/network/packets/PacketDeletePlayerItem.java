package mezz.jei.network.packets;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistries;

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
		buf.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, itemStack.getItem());
	}

	public static void readPacketData(PacketBuffer buf, PlayerEntity player) {
		Item item = buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
		if (CommandUtilServer.hasPermission(player)) {
			ItemStack playerItem = player.inventory.getCarried();
			if (playerItem.getItem() == item) {
				player.inventory.setCarried(ItemStack.EMPTY);
			}
		}
	}
}
