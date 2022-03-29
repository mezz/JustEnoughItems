package mezz.jei.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.util.ServerCommandUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

	public static void readPacketData(ServerPacketData data) {
		FriendlyByteBuf buf = data.buf();
		ServerPlayer player = data.player();
		IServerConfig serverConfig = data.serverConfig();
		Item item = buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
		if (ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig)) {
			ItemStack playerItem = player.containerMenu.getCarried();
			if (playerItem.getItem() == item) {
				player.containerMenu.setCarried(ItemStack.EMPTY);
			}
		}
	}
}
