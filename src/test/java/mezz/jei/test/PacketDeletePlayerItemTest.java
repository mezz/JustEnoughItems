package mezz.jei.test;

import java.lang.reflect.Field;

import io.netty.buffer.Unpooled;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import net.minecraft.command.CommandGive;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PacketDeletePlayerItemTest {
	@BeforeClass
	public static void bootstrapMinecraft() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
	}

	@Test
	public void rejectsDeletionWithoutGivePermission() throws ReflectiveOperationException {
		EntityPlayerMP player = createPlayer(false);
		ItemStack carried = new ItemStack(Items.APPLE, 3);
		player.inventory.setItemStack(carried);

		deleteThroughPacket(player, Items.APPLE);

		Assert.assertSame("An unprivileged packet must leave the carried stack intact", carried, player.inventory.getItemStack());
		Assert.assertEquals(3, carried.getCount());
	}

	@Test
	public void deletesMatchingItemWithGivePermission() throws ReflectiveOperationException {
		EntityPlayerMP player = createPlayer(true);
		player.inventory.setItemStack(new ItemStack(Items.APPLE, 3));

		deleteThroughPacket(player, Items.APPLE);

		Assert.assertTrue(player.inventory.getItemStack().isEmpty());
	}

	@Test
	public void ignoresStalePacketForADifferentCarriedItem() throws ReflectiveOperationException {
		EntityPlayerMP player = createPlayer(true);
		ItemStack carried = new ItemStack(Items.STICK, 2);
		player.inventory.setItemStack(carried);

		deleteThroughPacket(player, Items.APPLE);

		Assert.assertSame(carried, player.inventory.getItemStack());
	}

	private static EntityPlayerMP createPlayer(boolean canGive) throws ReflectiveOperationException {
		EntityPlayerMP player = mock(EntityPlayerMP.class);
		MinecraftServer server = mock(MinecraftServer.class);
		CommandHandler commands = new CommandHandler() {
			@Override
			protected MinecraftServer getServer() {
				return server;
			}
		};
		commands.registerCommand(new CommandGive());
		when(server.getCommandManager()).thenReturn(commands);
		when(player.canUseCommand(2, "give")).thenReturn(canGive);
		when(player.getName()).thenReturn("jei_test");
		EntityPlayer.class.getField("inventory").set(player, new InventoryPlayer(player));
		Field serverField = EntityPlayerMP.class.getField("server");
		serverField.setAccessible(true);
		serverField.set(player, server);
		return player;
	}

	private static void deleteThroughPacket(EntityPlayerMP player, Item item) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			new PacketDeletePlayerItem(new ItemStack(item)).writePacketData(buffer);
			PacketDeletePlayerItem.readPacketData(buffer, player);
		} finally {
			buffer.release();
		}
	}
}
