package mezz.jei.network.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import mezz.jei.util.CommandUtilServer;
import mezz.jei.util.GiveMode;

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
	public void writePacketData(PacketBuffer buf) {
		buf.writeItemStack(itemStack);
		buf.writeEnumValue(giveMode);
	}

	public static void readPacketData(PacketBuffer buf, PlayerEntity player) {
		if (player instanceof ServerPlayerEntity) {
			ServerPlayerEntity sender = (ServerPlayerEntity) player;

			ItemStack itemStack = buf.readItemStack();
			if (!itemStack.isEmpty()) {
				GiveMode giveMode = buf.readEnumValue(GiveMode.class);
				CommandUtilServer.executeGive(sender, itemStack, giveMode);
			}
		}
	}
}
