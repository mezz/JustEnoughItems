package mezz.jei.forge.tests.commands;

import mezz.jei.common.util.ServerCommandUtil;
import mezz.jei.forge.tests.lib.JeiGameTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("jei")
@PrefixGameTestTemplate(false)
public final class ServerCommandUtilGameTests {
	private ServerCommandUtilGameTests() {
	}

	@GameTest(template = "empty")
	public static void mousePickupUpdatesServerPlayerMenuWithModdedSlots(GameTestHelper gameTestHelper) {
		JeiGameTestHelper helper = new JeiGameTestHelper(gameTestHelper);
		// Setup: a real ServerPlayer has a menu with additional slots registered through the normal menu API.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, ItemStack.EMPTY, 8);
		helper.assertEquals(8, menu.slots.size(), "Expected the ServerPlayer menu to contain the modded slots");

		// Operation: give an item stack through the ServerPlayer mouse-pickup path.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		// Assertions: the intended cursor update and server synchronization both complete without crashing.
		helper.assertSameStack(new ItemStack(Items.APPLE, 3), menu.getCarried(), "Expected carried stack");
		helper.assertEquals(1, menu.broadcastChangeCount, "Expected the ServerPlayer menu to broadcast the cursor change");
		helper.succeed();
	}

	private static TestMenu openMenu(ServerPlayer player, ItemStack carriedStack, int slotCount) {
		TestMenu menu = new TestMenu(slotCount);
		player.containerMenu = menu;
		menu.setCarried(carriedStack.copy());
		return menu;
	}

	private static class TestMenu extends AbstractContainerMenu {
		private int broadcastChangeCount;

		private TestMenu(int slotCount) {
			super(null, 0);
			SimpleContainer moddedInventory = new SimpleContainer(slotCount);
			for (int i = 0; i < slotCount; i++) {
				addSlot(new Slot(moddedInventory, i, 0, 0));
			}
		}

		@Override
		public void broadcastChanges() {
			this.broadcastChangeCount++;
			super.broadcastChanges();
		}

		@Override
		public ItemStack quickMoveStack(Player player, int slotIndex) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}
	}
}
