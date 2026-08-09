package mezz.jei.forge.tests.recipe.transfer;

import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

@GameTestHolder("jei")
@PrefixGameTestTemplate(false)
public final class RecipeTransferGameTests {
	private RecipeTransferGameTests() {
	}

	@GameTest(template = "empty")
	public static void transfersIntoEmptyItemHandlerSlot(GameTestHelper helper) {
		Player player = helper.makeMockPlayer();
		TestMenu menu = new TestMenu(player, (testMenu, testPlayer) ->
			new SlotItemHandler(new ItemStackHandler(1), 0, 0, 0)
		);
		menu.inventorySlot.set(new ItemStack(Items.OAK_PLANKS));
		assertTrue(
			helper,
			!menu.targetSlot.allowModification(player),
			"Expected the empty item-handler slot to reject modification of its current contents"
		);

		transfer(player, menu);

		assertTrue(helper, menu.targetSlot.getItem().is(Items.OAK_PLANKS), "Expected the ingredient in the item-handler slot");
		assertTrue(helper, menu.inventorySlot.getItem().isEmpty(), "Expected the source inventory slot to be empty");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void doesNotInsertIntoRejectingItemHandlerSlot(GameTestHelper helper) {
		Player player = helper.makeMockPlayer();
		ItemStackHandler rejectingItemHandler = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		TestMenu menu = new TestMenu(player, (testMenu, testPlayer) ->
			new SlotItemHandler(rejectingItemHandler, 0, 0, 0)
		);
		menu.inventorySlot.set(new ItemStack(Items.OAK_PLANKS));

		transfer(player, menu);

		assertTrue(helper, menu.targetSlot.getItem().isEmpty(), "Expected the rejecting item-handler slot to remain empty");
		assertTrue(helper, menu.inventorySlot.getItem().is(Items.OAK_PLANKS), "Expected the source ingredient to be restored");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void doesNotInsertIntoCraftingResultSlot(GameTestHelper helper) {
		Player player = helper.makeMockPlayer();
		TestMenu menu = new TestMenu(player, (testMenu, testPlayer) ->
			new ResultSlot(
				testPlayer,
				new CraftingContainer(testMenu, 1, 1),
				new ResultContainer(),
				0,
				0,
				0
			)
		);
		menu.inventorySlot.set(new ItemStack(Items.OAK_PLANKS));

		transfer(player, menu);

		assertTrue(helper, menu.targetSlot.getItem().isEmpty(), "Expected the crafting result slot to remain empty");
		assertTrue(helper, menu.inventorySlot.getItem().is(Items.OAK_PLANKS), "Expected the source ingredient to be restored");
		helper.succeed();
	}

	private static void transfer(Player player, TestMenu menu) {
		player.containerMenu = menu;
		BasicRecipeTransferHandlerServer.setItems(
			player,
			List.of(new TransferOperation(menu.inventorySlot.index, menu.targetSlot.index)),
			List.of(menu.targetSlot),
			List.of(menu.inventorySlot),
			false,
			true
		);
	}

	private static void assertTrue(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			helper.fail(message);
		}
	}

	@FunctionalInterface
	private interface TargetSlotFactory {
		Slot create(TestMenu menu, Player player);
	}

	private static final class TestMenu extends AbstractContainerMenu {
		private final Slot targetSlot;
		private final Slot inventorySlot;

		private TestMenu(Player player, TargetSlotFactory targetSlotFactory) {
			super(null, 0);
			this.targetSlot = addSlot(targetSlotFactory.create(this, player));
			this.inventorySlot = addSlot(new Slot(player.getInventory(), 0, 0, 0));
		}

		@Override
		public ItemStack quickMoveStack(Player player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}
	}
}
