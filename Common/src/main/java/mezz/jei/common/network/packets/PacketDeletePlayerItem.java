package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.util.ServerCommandUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PacketDeletePlayerItem extends PlayToServerPacket<PacketDeletePlayerItem> {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final CustomPacketPayload.Type<PacketDeletePlayerItem> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "delete_player_item"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketDeletePlayerItem> STREAM_CODEC = StreamCodec.composite(
		ItemStack.STREAM_CODEC,
		p -> p.itemStack,
		PacketDeletePlayerItem::new
	);

	private final ItemStack itemStack;

	public PacketDeletePlayerItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public Type<PacketDeletePlayerItem> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketDeletePlayerItem> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		ServerPlayer player = context.player();
		IServerConfig serverConfig = context.serverConfig();
		if (ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig)) {
			ItemStack playerItem = player.containerMenu.getCarried();
			if (playerItem.getItem() == itemStack.getItem()) {
				player.containerMenu.setCarried(ItemStack.EMPTY);
			} else {
				LOGGER.warn("Player '{} ({})' tried to delete ItemStack '{}' but is currently holding a different ItemStack '{}'.", player.getName(), player.getUUID(), itemStack.getDisplayName(), playerItem.getDisplayName());
			}
		} else {
			ItemStack playerItem = player.containerMenu.getCarried();
			LOGGER.error("Player '{} ({})' tried to delete ItemStack '{}' but does not have permission.", player.getName(), player.getUUID(), playerItem.getDisplayName());
			IConnectionToClient connection = context.connection();
			connection.sendPacketToClient(new PacketCheatPermission(false, serverConfig), player);
		}
	}
}
