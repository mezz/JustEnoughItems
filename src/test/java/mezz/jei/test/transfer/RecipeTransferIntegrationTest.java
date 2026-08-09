package mezz.jei.test.transfer;

import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RecipeTransferIntegrationTest {
	@BeforeClass
	public static void bootstrapMinecraft() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
	}

	@Test
	public void transfersIntoEmptyItemHandlerSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		TestContainer container = new TestContainer(player, (testContainer, testPlayer) ->
			new SlotItemHandler(new ItemStackHandler(1), 0, 0, 0)
		);
		container.inventorySlot.putStack(new ItemStack(Items.STICK));
		assertFalse("Expected the empty item-handler slot to reject extraction", container.targetSlot.canTakeStack(player));

		transfer(player, container);

		assertSame("Expected the ingredient in the item-handler slot", Items.STICK, container.targetSlot.getStack().getItem());
		assertTrue("Expected the source inventory slot to be empty", container.inventorySlot.getStack().isEmpty());
	}

	@Test
	public void doesNotInsertIntoRejectingItemHandlerSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		ItemStackHandler rejectingItemHandler = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		TestContainer container = new TestContainer(player, (testContainer, testPlayer) ->
			new SlotItemHandler(rejectingItemHandler, 0, 0, 0)
		);
		container.inventorySlot.putStack(new ItemStack(Items.STICK));

		transfer(player, container);

		assertTrue("Expected the rejecting item-handler slot to remain empty", container.targetSlot.getStack().isEmpty());
		assertSame("Expected the source ingredient to be restored", Items.STICK, container.inventorySlot.getStack().getItem());
	}

	@Test
	public void doesNotInsertIntoCraftingResultSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		TestContainer container = new TestContainer(player, (testContainer, testPlayer) ->
			new SlotCrafting(
				testPlayer,
				new InventoryCrafting(testContainer, 1, 1),
				new InventoryCraftResult(),
				0,
				0,
				0
			)
		);
		container.inventorySlot.putStack(new ItemStack(Items.STICK));

		transfer(player, container);

		assertTrue("Expected the crafting result slot to remain empty", container.targetSlot.getStack().isEmpty());
		assertSame("Expected the source ingredient to be restored", Items.STICK, container.inventorySlot.getStack().getItem());
	}

	private static void transfer(EntityPlayer player, TestContainer container) {
		player.openContainer = container;
		BasicRecipeTransferHandlerServer.setItems(
			player,
			Collections.singletonMap(0, container.inventorySlot.slotNumber),
			Collections.emptyMap(),
			Collections.singletonList(container.targetSlot.slotNumber),
			Collections.singletonList(container.inventorySlot.slotNumber),
			false,
			true
		);
	}

	@FunctionalInterface
	private interface TargetSlotFactory {
		Slot create(TestContainer container, EntityPlayer player);
	}

	private static final class TestContainer extends Container {
		private final Slot targetSlot;
		private final Slot inventorySlot;

		private TestContainer(EntityPlayer player, TargetSlotFactory targetSlotFactory) {
			this.targetSlot = addSlotToContainer(targetSlotFactory.create(this, player));
			this.inventorySlot = addSlotToContainer(new Slot(new InventoryBasic("RecipeTransferTest", false, 1), 0, 0, 0));
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return true;
		}
	}
}
