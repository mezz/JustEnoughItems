package mezz.jei.test.transfer;

import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class RecipeTransferIntegrationTest {
	@BeforeAll
	public static void bootstrapMinecraft() {
		Bootstrap.bootStrap();
	}

	@Test
	public void transfersIntoEmptyItemHandlerSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		TestContainer container = new TestContainer(player, (testContainer, testPlayer) ->
			new SlotItemHandler(new ItemStackHandler(1), 0, 0, 0)
		);
		container.inventorySlot.set(new ItemStack(Items.OAK_PLANKS));
		assertFalse(container.targetSlot.mayPickup(player), "Expected the empty item-handler slot to reject extraction");

		transfer(player, container);

		assertSame(Items.OAK_PLANKS, container.targetSlot.getItem().getItem(), "Expected the ingredient in the item-handler slot");
		assertTrue(container.inventorySlot.getItem().isEmpty(), "Expected the source inventory slot to be empty");
	}

	@Test
	public void doesNotInsertIntoRejectingItemHandlerSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		ItemStackHandler rejectingItemHandler = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		TestContainer container = new TestContainer(player, (testContainer, testPlayer) ->
			new SlotItemHandler(rejectingItemHandler, 0, 0, 0)
		);
		container.inventorySlot.set(new ItemStack(Items.OAK_PLANKS));

		transfer(player, container);

		assertTrue(container.targetSlot.getItem().isEmpty(), "Expected the rejecting item-handler slot to remain empty");
		assertSame(Items.OAK_PLANKS, container.inventorySlot.getItem().getItem(), "Expected the source ingredient to be restored");
	}

	@Test
	public void doesNotInsertIntoCraftingResultSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		TestContainer container = new TestContainer(player, (testContainer, testPlayer) ->
			new CraftingResultSlot(
				testPlayer,
				new CraftingInventory(testContainer, 1, 1),
				new CraftResultInventory(),
				0,
				0,
				0
			)
		);
		container.inventorySlot.set(new ItemStack(Items.OAK_PLANKS));

		transfer(player, container);

		assertTrue(container.targetSlot.getItem().isEmpty(), "Expected the crafting result slot to remain empty");
		assertSame(Items.OAK_PLANKS, container.inventorySlot.getItem().getItem(), "Expected the source ingredient to be restored");
	}

	private static void transfer(PlayerEntity player, TestContainer container) {
		player.containerMenu = container;
		BasicRecipeTransferHandlerServer.setItems(
			player,
			Collections.singletonMap(0, container.inventorySlot.index),
			Collections.singletonList(container.targetSlot.index),
			Collections.singletonList(container.inventorySlot.index),
			false,
			true
		);
	}

	@FunctionalInterface
	private interface TargetSlotFactory {
		Slot create(TestContainer container, PlayerEntity player);
	}

	private static final class TestContainer extends Container {
		private final Slot targetSlot;
		private final Slot inventorySlot;

		private TestContainer(PlayerEntity player, TargetSlotFactory targetSlotFactory) {
			super(null, 0);
			this.targetSlot = addSlot(targetSlotFactory.create(this, player));
			this.inventorySlot = addSlot(new Slot(new Inventory(1), 0, 0, 0));
		}

		@Override
		public ItemStack quickMoveStack(PlayerEntity player, int index) {
			return ItemStack.EMPTY;
		}

		@Override
		public boolean stillValid(PlayerEntity player) {
			return true;
		}
	}
}
