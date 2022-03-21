package mezz.jei.network.packets;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
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
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, itemStack.getItem());
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player) {
		Item item = buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
		if (CommandUtilServer.hasPermissionForCheatMode(player)) {
			ItemStack playerItem = player.containerMenu.getCarried();
			if (playerItem.getItem() == item) {
				player.containerMenu.setCarried(ItemStack.EMPTY);
			}
		}
	}
}
