package mezz.jei.common.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

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
		ServerPacketContext context = data.context();
		ServerPlayer player = context.player();
		IServerConfig serverConfig = context.serverConfig();
		int itemId = buf.readVarInt();
		Optional<Item> value = registry.getValue(itemId);
		if (value.isPresent() && ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig)) {
			ItemStack playerItem = player.containerMenu.getCarried();
			if (playerItem.getItem() == value.get()) {
				player.containerMenu.setCarried(ItemStack.EMPTY);
			}
		}
	}
}
