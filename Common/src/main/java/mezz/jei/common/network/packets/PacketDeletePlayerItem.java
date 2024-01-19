package mezz.jei.common.network.packets;

import mezz.jei.common.network.*;
import mezz.jei.common.platform.Services;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PacketDeletePlayerItem extends PacketJeiToServer {
	private static final Logger LOGGER = LogManager.getLogger();

	private final int itemID;

	public PacketDeletePlayerItem(Item item) {
		this(Services.PLATFORM.getRegistry(Registries.ITEM).getId(item));
	}

	public PacketDeletePlayerItem(int itemID) {
		this.itemID = itemID;
	}

	@Override
	public PacketIdServer getPacketId() {
		return PacketIdServer.DELETE_ITEM;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(itemID);
	}

	public static PacketDeletePlayerItem readPacketData(FriendlyByteBuf buf) {
		int itemId = buf.readVarInt();
		return new PacketDeletePlayerItem(itemId);
	}

	@Override
	public void processOnServerThread(ServerPacketContext context) {
		Item item = Services.PLATFORM
				.getRegistry(Registries.ITEM)
				.getValue(itemID)
				.orElse(null);
		ServerPlayer player = context.player();
		if (item == null) {
			LOGGER.debug("Player '{} ({})' tried to delete Item ID '{}' but no item is registered with that ID.", player.getName(), player.getUUID(), itemID);
		} else {
			IServerConfig serverConfig = context.serverConfig();
			if (ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig)) {
				ItemStack playerItem = player.containerMenu.getCarried();
				if (playerItem.getItem() == item) {
					player.containerMenu.setCarried(ItemStack.EMPTY);
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Player '{} ({})' tried to delete Item '{}' but is currently holding a different ItemStack '{}'.", player.getName(), player.getUUID(), item, playerItem.getDisplayName());
					}
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					ItemStack playerItem = player.containerMenu.getCarried();
					LOGGER.debug("Player '{} ({})' tried to delete ItemStack '{}' but does not have permission.", player.getName(), player.getUUID(), playerItem.getDisplayName());
				}
				IConnectionToClient connection = context.connection();
				connection.sendPacketToClient(new PacketCheatPermission(false), player);
			}
		}
	}
}
