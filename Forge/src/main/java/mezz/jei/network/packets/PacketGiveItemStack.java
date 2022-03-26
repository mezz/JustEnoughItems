package mezz.jei.network.packets;

import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;
import mezz.jei.core.config.GiveMode;

public class PacketGiveItemStack extends PacketJei {
	private final ItemStack itemStack;
	private final GiveMode giveMode;

	public PacketGiveItemStack(ItemStack itemStack, GiveMode giveMode) {
		this.itemStack = itemStack;
		this.giveMode = giveMode;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.GIVE_ITEM;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeItem(itemStack);
		buf.writeEnum(giveMode);
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player) {
		if (player instanceof ServerPlayer sender) {

			ItemStack itemStack = buf.readItem();
			if (!itemStack.isEmpty()) {
				GiveMode giveMode = buf.readEnum(GiveMode.class);
				CommandUtilServer.executeGive(sender, itemStack, giveMode);
			}
		}
	}
}
