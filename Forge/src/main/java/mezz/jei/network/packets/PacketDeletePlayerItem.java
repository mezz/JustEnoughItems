package mezz.jei.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.util.ServerCommandUtil;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
		IPlatformRegistry<Item> registry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		int itemId = registry.getId(itemStack.getItem());
		buf.writeVarInt(itemId);
	}

	public static void readPacketData(ServerPacketData data) {
		IPlatformRegistry<Item> registry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		FriendlyByteBuf buf = data.buf();
		ServerPlayer player = data.player();
		IServerConfig serverConfig = data.serverConfig();
		int itemId = buf.readVarInt();
		Item item = registry.getValue(itemId);
		if (ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig)) {
			ItemStack playerItem = player.containerMenu.getCarried();
			if (playerItem.getItem() == item) {
				player.containerMenu.setCarried(ItemStack.EMPTY);
			}
		}
	}
}
