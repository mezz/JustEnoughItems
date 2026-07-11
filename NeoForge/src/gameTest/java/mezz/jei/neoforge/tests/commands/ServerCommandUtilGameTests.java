package mezz.jei.neoforge.tests.commands;

import mezz.jei.common.util.ServerCommandUtil;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "commands")
public final class ServerCommandUtilGameTests {
	private ServerCommandUtilGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mouse pickup places an item stack on an empty cursor.")
	public static void mousePickupPlacesItemWhenCursorEmpty(JeiGameTestHelper helper) {
		// Setup: the player has an empty carried stack.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, ItemStack.EMPTY);

		// Operation: give an item stack through mouse pickup mode.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		// Assertions: the cursor receives the stack and the menu broadcasts the change.
		assertCarried(helper, menu, Items.APPLE, 3);
		helper.assertEquals(1, menu.broadcastChangeCount, "Expected mouse pickup to broadcast the cursor change");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mouse pickup stacks matching items on the cursor.")
	public static void mousePickupStacksMatchingItem(JeiGameTestHelper helper) {
		// Setup: the cursor already carries a partial stack of the requested item.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, new ItemStack(Items.APPLE, 60));

		// Operation: give more of the same item through mouse pickup mode.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		// Assertions: the cursor stack grows by the requested amount.
		assertCarried(helper, menu, Items.APPLE, 63);
		helper.assertEquals(1, menu.broadcastChangeCount, "Expected mouse pickup to broadcast the cursor change");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mouse pickup caps matching cursor stacks at their max stack size.")
	public static void mousePickupCapsMatchingStackAtMaxSize(JeiGameTestHelper helper) {
		// Setup: the cursor can accept only part of the requested stack.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, new ItemStack(Items.APPLE, 63));

		// Operation: give more items than the cursor can accept.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		// Assertions: the cursor reaches its max stack size and broadcasts exactly one change.
		assertCarried(helper, menu, Items.APPLE, 64);
		helper.assertEquals(1, menu.broadcastChangeCount, "Expected mouse pickup to broadcast the cursor change");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mouse pickup does nothing when a matching cursor stack is full.")
	public static void mousePickupDoesNothingWhenMatchingCursorStackIsFull(JeiGameTestHelper helper) {
		// Setup: the cursor already carries a full stack of the requested item.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, new ItemStack(Items.APPLE, 64));

		// Operation: try to give more of the same item through mouse pickup mode.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		// Assertions: nothing changes, so the menu is not broadcast as a successful give.
		assertCarried(helper, menu, Items.APPLE, 64);
		helper.assertEquals(0, menu.broadcastChangeCount, "Expected full cursor pickup to be a no-op");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mouse pickup does nothing when a matching unstackable item is already carried.")
	public static void mousePickupDoesNothingWhenMatchingUnstackableCursorItemIsFull(JeiGameTestHelper helper) {
		// Setup: the cursor carries a full unstackable item.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, new ItemStack(Items.DIAMOND_SWORD));

		// Operation: try to give the same unstackable item through mouse pickup mode.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.DIAMOND_SWORD));

		// Assertions: the full unstackable cursor item is left alone and no successful give is broadcast.
		assertCarried(helper, menu, Items.DIAMOND_SWORD, 1);
		helper.assertEquals(0, menu.broadcastChangeCount, "Expected full unstackable cursor pickup to be a no-op");
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Mouse pickup replaces a different cursor item.")
	public static void mousePickupReplacesDifferentCursorItem(JeiGameTestHelper helper) {
		// Setup: the cursor carries a different item from the requested stack.
		ServerPlayer player = helper.getPlayer();
		TestMenu menu = openMenu(player, new ItemStack(Items.CARROT, 2));

		// Operation: give a different item through mouse pickup mode.
		ServerCommandUtil.mousePickupItemStack(player, new ItemStack(Items.APPLE, 3));

		// Assertions: the cursor switches to the requested stack and broadcasts the change.
		assertCarried(helper, menu, Items.APPLE, 3);
		helper.assertEquals(1, menu.broadcastChangeCount, "Expected replacing cursor stack to broadcast the change");
		helper.succeed();
	}

	private static TestMenu openMenu(ServerPlayer player, ItemStack carriedStack) {
		TestMenu menu = new TestMenu();
		player.containerMenu = menu;
		menu.setCarried(carriedStack.copy());
		return menu;
	}

	private static void assertCarried(JeiGameTestHelper helper, TestMenu menu, Item item, int count) {
		helper.assertSameStack(new ItemStack(item, count), menu.getCarried(), "Expected carried stack");
	}

	private static class TestMenu extends AbstractContainerMenu {
		private int broadcastChangeCount;

		private TestMenu() {
			super(null, 0);
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
