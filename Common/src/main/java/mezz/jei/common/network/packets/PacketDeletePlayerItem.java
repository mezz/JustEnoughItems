package mezz.jei.common.network.packets;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class PacketDeletePlayerItem extends PacketJei {
	private static final Logger LOGGER = LogManager.getLogger();

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
		IPlatformRegistry<Item> registry = Services.PLATFORM.getRegistry(Registries.ITEM);
		int itemId = registry.getId(itemStack.getItem());
		buf.writeVarInt(itemId);
	}

	public static CompletableFuture<Void> readPacketData(ServerPacketData data) {
		FriendlyByteBuf buf = data.buf();
		ServerPacketContext context = data.context();
		ServerPlayer player = context.player();
		int itemId = buf.readVarInt();

		return Services.PLATFORM
			.getRegistry(Registries.ITEM)
			.getValue(itemId)
			.map(item -> {
				MinecraftServer server = player.server;
				return server.submit(() -> deletePlayerItem(player, context, item));
			})
			.orElseGet(() -> {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Player '{} ({})' tried to delete Item ID '{}' but no item is registered with that ID.", player.getName(), player.getUUID(), itemId);
				}
				return CompletableFuture.completedFuture(null);
			});
	}

	private static void deletePlayerItem(ServerPlayer player, ServerPacketContext context, Item item) {
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
