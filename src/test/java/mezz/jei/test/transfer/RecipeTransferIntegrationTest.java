package mezz.jei.test.transfer;

import io.netty.buffer.Unpooled;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.startup.StackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RecipeTransferIntegrationTest {
	private static final StackHelper STACK_HELPER = new StackHelper(mock(ISubtypeRegistry.class));

	@BeforeClass
	public static void bootstrapMinecraft() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
	}

	@Test
	public void transfersRecipeThroughPacketToCraftingGrid() {
		TestContext context = createContext(2, 3);
		context.setInventory(0, Items.PAPER, 1);
		context.setInventory(1, Items.STICK, 1);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(ingredient(Items.PAPER), ingredient(Items.STICK)),
			false,
			true
		);

		assertTrue(result.missingItems.isEmpty());
		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0), context.targetSlots.get(1));
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void transfersIntoEmptyItemHandlerSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, EntityPlayer testPlayer, int index) {
				return new SlotItemHandler(new ItemStackHandler(1), 0, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.PAPER, 1);
		assertFalse("Expected the empty item-handler slot to reject extraction", context.targetSlots.get(0).canTakeStack(player));

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotInsertIntoRejectingItemHandlerSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		final ItemStackHandler rejectingItemHandler = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, EntityPlayer testPlayer, int index) {
				return new SlotItemHandler(rejectingItemHandler, 0, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotInsertIntoRejectingSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, EntityPlayer testPlayer, int index) {
				return new Slot(new InventoryBasic("test", false, 1), 0, 0, 0) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return false;
					}
				};
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotInsertIntoCraftingResultSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, EntityPlayer testPlayer, int index) {
				return new SlotCrafting(
					testPlayer,
					new InventoryCrafting(testContainer, 1, 1),
					new InventoryCraftResult(),
					0,
					0,
					0
				);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void transfersFromSplitStacks() {
		TestContext context = createContext(3, 2);
		context.setInventory(0, Items.PAPER, 2);
		context.setInventory(1, Items.PAPER, 1);

		transfer(
			context,
			recipe(ingredient(Items.PAPER), ingredient(Items.PAPER), ingredient(Items.PAPER)),
			false,
			true
		);

		for (Slot targetSlot : context.targetSlots) {
			assertStack(targetSlot, Items.PAPER, 1);
		}
		assertAllOtherSlotsEmpty(context, context.targetSlots.toArray(new Slot[0]));
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void transfersStackedIngredientFromSingleStack() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 6);

		transfer(context, recipe(countedIngredient(4, Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 4);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 2);
		assertEquals(6, totalItemCount(context));
	}

	@Test
	public void transfersStackedIngredientFromSplitStacks() {
		TestContext context = createContext(1, 2);
		context.setInventory(0, Items.PAPER, 2);
		context.setInventory(1, Items.PAPER, 1);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(countedIngredient(3, Items.PAPER)),
			false,
			true
		);

		assertTrue(result.missingItems.isEmpty());
		assertStack(context.targetSlots.get(0), Items.PAPER, 3);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void transfersStackedIngredientFromSparseInventorySlots() {
		TestContext context = createContext(1, 8);
		context.setInventory(0, Items.FLINT, 11);
		context.setInventory(2, Items.PAPER, 2);
		context.setInventory(5, Items.STICK, 5);
		context.setInventory(7, Items.PAPER, 1);

		transfer(context, recipe(countedIngredient(3, Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 3);
		assertStack(context.inventorySlots.get(0), Items.FLINT, 11);
		assertStack(context.inventorySlots.get(5), Items.STICK, 5);
		assertTrue(context.inventorySlots.get(2).getStack().isEmpty());
		assertTrue(context.inventorySlots.get(7).getStack().isEmpty());
		assertEquals(19, totalItemCount(context));
	}

	@Test
	public void fillsStackedIngredientAlreadyInTargetSlot() {
		TestContext context = createContext(1, 1);
		context.setTarget(0, Items.PAPER, 2);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(countedIngredient(3, Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 3);
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void transfersStackedIngredientsToMultipleSlotsFromSharedStacks() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.PAPER, 3);
		context.setInventory(1, Items.PAPER, 1);

		transfer(
			context,
			recipe(countedIngredient(2, Items.PAPER), countedIngredient(2, Items.PAPER)),
			false,
			true
		);

		assertStack(context.targetSlots.get(0), Items.PAPER, 2);
		assertStack(context.targetSlots.get(1), Items.PAPER, 2);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0), context.targetSlots.get(1));
		assertEquals(4, totalItemCount(context));
	}

	@Test
	public void movesStackedIngredientFromWrongCraftingSlot() {
		TestContext context = createContext(2, 1);
		context.setTarget(0, Items.PAPER, 2);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(emptyIngredient(), countedIngredient(3, Items.PAPER)), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.targetSlots.get(1), Items.PAPER, 3);
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void reportsMissingStackedIngredientCount() {
		TestContext context = createContext(1, 2);
		context.setInventory(0, Items.PAPER, 1);
		context.setInventory(1, Items.PAPER, 1);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(countedIngredient(3, Items.PAPER)),
			false,
			true
		);

		assertEquals(Collections.singletonList(0), result.missingItems);
		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertStack(context.inventorySlots.get(1), Items.PAPER, 1);
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void transfersAlternativeStackedIngredientWithDifferentCounts() {
		TestContext context = createContext(1, 2);
		context.setInventory(0, Items.PAPER, 2);
		context.setInventory(1, Items.STRING, 2);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(ingredientStacks(new ItemStack(Items.PAPER, 3), new ItemStack(Items.STRING, 2))),
			false,
			true
		);

		assertTrue(result.missingItems.isEmpty());
		assertStack(context.targetSlots.get(0), Items.STRING, 2);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 2);
		assertTrue(context.inventorySlots.get(1).getStack().isEmpty());
		assertEquals(4, totalItemCount(context));
	}

	@Test
	public void reportsMissingAlternativeStackedIngredientCount() {
		TestContext context = createContext(1, 2);
		context.setInventory(0, Items.PAPER, 2);
		context.setInventory(1, Items.STRING, 1);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(ingredientStacks(new ItemStack(Items.PAPER, 3), new ItemStack(Items.STRING, 2))),
			false,
			true
		);

		assertEquals(Collections.singletonList(0), result.missingItems);
		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 2);
		assertStack(context.inventorySlots.get(1), Items.STRING, 1);
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void maxTransferMovesStackedIngredientCompleteSets() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.PAPER, 5);
		context.setInventory(1, Items.STICK, 3);

		transfer(
			context,
			recipe(countedIngredient(2, Items.PAPER), ingredient(Items.STICK)),
			true,
			true
		);

		assertStack(context.targetSlots.get(0), Items.PAPER, 4);
		assertStack(context.targetSlots.get(1), Items.STICK, 2);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertStack(context.inventorySlots.get(1), Items.STICK, 1);
		assertEquals(8, totalItemCount(context));
	}

	@Test
	public void maxTransferStackedIngredientStopsAtSlotLimit() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 64);

		transfer(context, recipe(countedIngredient(3, Items.PAPER)), true, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 63);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(64, totalItemCount(context));
	}

	@Test
	public void stowsDisplacedCountedCraftingStackIntoInventory() {
		TestContext context = createContext(1, 3);
		context.setTarget(0, Items.FLINT, 24);
		context.setInventory(0, Items.PAPER, 3);
		context.setInventory(1, Items.FLINT, 40);
		context.setInventory(2, Items.STICK, 64);

		transfer(context, recipe(countedIngredient(3, Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 3);
		assertStack(context.inventorySlots.get(1), Items.FLINT, 64);
		assertStack(context.inventorySlots.get(2), Items.STICK, 64);
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertEquals(131, totalItemCount(context));
	}

	@Test
	public void rollsBackIncompleteCountedCompleteSet() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.PAPER, 5);
		context.setInventory(1, Items.STICK, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).slotNumber);
		recipeMap.put(1, context.inventorySlots.get(1).slotNumber);
		Map<Integer, Integer> recipeCountMap = new LinkedHashMap<>();
		recipeCountMap.put(0, 2);
		recipeCountMap.put(1, 1);

		sendPacket(
			context,
			recipeMap,
			recipeCountMap,
			slotIndexes(context.targetSlots),
			slotIndexes(context.inventorySlots),
			true,
			true
		);

		assertStack(context.targetSlots.get(0), Items.PAPER, 2);
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 3);
		assertTrue(context.inventorySlots.get(1).getStack().isEmpty());
		assertEquals(6, totalItemCount(context));
	}

	@Test
	public void transfersAlternativeIngredient() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.STRING, 1);

		transfer(context, recipe(ingredient(Items.PAPER, Items.STRING)), false, true);

		assertStack(context.targetSlots.get(0), Items.STRING, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
	}

	@Test
	public void transfersAlternativeWithoutStealingRequiredIngredient() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.PAPER, 1);
		context.setInventory(1, Items.STICK, 1);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(ingredient(Items.PAPER, Items.STICK), ingredient(Items.PAPER)),
			false,
			true
		);

		assertTrue(result.missingItems.isEmpty());
		assertStack(context.targetSlots.get(0), Items.STICK, 1);
		assertStack(context.targetSlots.get(1), Items.PAPER, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0), context.targetSlots.get(1));
	}

	@Test
	public void reportsMissingIngredientsWithoutChangingMenu() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.PAPER, 1);

		StackHelper.MatchingItemsResult result = transfer(
			context,
			recipe(ingredient(Items.PAPER), ingredient(Items.STICK)),
			false,
			true
		);

		assertEquals(Collections.singletonList(1), result.missingItems);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertTrue(context.targetSlots.get(1).getStack().isEmpty());
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maxTransferMovesMultipleCompleteSets() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 5);

		transfer(context, recipe(ingredient(Items.PAPER)), true, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 5);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
		assertEquals(5, totalItemCount(context));
	}

	@Test
	public void maxTransferStopsAtLimitingIngredient() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.PAPER, 5);
		context.setInventory(1, Items.STICK, 2);

		transfer(context, recipe(ingredient(Items.PAPER), ingredient(Items.STICK)), true, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 2);
		assertStack(context.targetSlots.get(1), Items.STICK, 2);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 3);
		assertTrue(context.inventorySlots.get(1).getStack().isEmpty());
		assertEquals(7, totalItemCount(context));
	}

	@Test
	public void maxTransferStopsAtNonStackableLimit() {
		TestContext context = createContext(1, 2);
		context.setInventory(0, Items.DIAMOND_SWORD, 1);
		context.setInventory(1, Items.DIAMOND_SWORD, 1);

		transfer(context, recipe(ingredient(Items.DIAMOND_SWORD)), true, true);

		assertStack(context.targetSlots.get(0), Items.DIAMOND_SWORD, 1);
		assertEquals(1, inventoryCount(context, Items.DIAMOND_SWORD));
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void maxTransferStopsAtLowStackLimitIngredient() {
		TestContext context = createContext(2, 3);
		context.setInventory(0, Items.ENDER_PEARL, 16);
		context.setInventory(1, Items.ENDER_PEARL, 4);
		context.setInventory(2, Items.PAPER, 20);

		transfer(
			context,
			recipe(ingredient(Items.ENDER_PEARL), ingredient(Items.PAPER)),
			true,
			true
		);

		assertStack(context.targetSlots.get(0), Items.ENDER_PEARL, 16);
		assertStack(context.targetSlots.get(1), Items.PAPER, 16);
		assertEquals(4, inventoryCount(context, Items.ENDER_PEARL));
		assertEquals(4, inventoryCount(context, Items.PAPER));
		assertEquals(40, totalItemCount(context));
	}

	@Test
	public void maxTransferStopsAtNonStackableIngredientInMultiIngredientRecipe() {
		TestContext context = createContext(2, 3);
		context.setInventory(0, Items.DIAMOND_SWORD, 1);
		context.setInventory(1, Items.DIAMOND_SWORD, 1);
		context.setInventory(2, Items.PAPER, 10);

		transfer(
			context,
			recipe(ingredient(Items.DIAMOND_SWORD), ingredient(Items.PAPER)),
			true,
			true
		);

		assertStack(context.targetSlots.get(0), Items.DIAMOND_SWORD, 1);
		assertStack(context.targetSlots.get(1), Items.PAPER, 1);
		assertEquals(1, inventoryCount(context, Items.DIAMOND_SWORD));
		assertEquals(9, inventoryCount(context, Items.PAPER));
		assertEquals(12, totalItemCount(context));
	}

	@Test
	public void stowsDisplacedCraftingItemInInventory() {
		TestContext context = createContext(2, 3);
		context.setTarget(0, Items.FLINT, 1);
		context.setInventory(0, Items.PAPER, 1);
		context.setInventory(1, Items.STICK, 1);

		transfer(context, recipe(ingredient(Items.PAPER), ingredient(Items.STICK)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertEquals(1, inventoryCount(context, Items.FLINT));
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void stowsDisplacedCraftingItemIntoMatchingInventoryStack() {
		TestContext context = createContext(1, 2);
		context.setTarget(0, Items.FLINT, 1);
		context.setInventory(0, Items.PAPER, 1);
		context.setInventory(1, Items.FLINT, 63);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(1), Items.FLINT, 64);
		assertEquals(65, totalItemCount(context));
	}

	@Test
	public void doesNotStowDisplacedItemIntoRejectingInventorySlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		final InventoryBasic inventory = new InventoryBasic("test", false, 2);
		TestContainer container = new TestContainer(player, 1, 2, normalTargetFactory(), new InventorySlotFactory() {
			@Override
			public Slot create(int index) {
				if (index == 0) {
					return new Slot(inventory, index, 0, 0) {
						@Override
						public boolean isItemValid(ItemStack stack) {
							return stack.getItem() != Items.FLINT;
						}
					};
				}
				return new Slot(inventory, index, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setTarget(0, Items.FLINT, 1);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(1), Items.FLINT, 1);
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void transfersIngredientAlreadyInTargetSlot() {
		TestContext context = createContext(1, 1);
		context.setTarget(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotOverwriteLockedOccupiedTargetSlot() {
		EntityPlayer player = mock(EntityPlayer.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, EntityPlayer testPlayer, int index) {
				return new Slot(new InventoryBasic("test", false, 1), 0, 0, 0) {
					@Override
					public boolean canTakeStack(EntityPlayer player) {
						return false;
					}
				};
			}
		});
		TestContext context = new TestContext(player, container);
		context.setTarget(0, Items.FLINT, 1);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.FLINT, 1);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void movesIngredientFromWrongCraftingSlot() {
		TestContext context = createContext(2, 1);
		context.setTarget(0, Items.STICK, 1);

		transfer(context, recipe(emptyIngredient(), ingredient(Items.STICK)), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(1));
	}

	@Test
	public void transfersFromMovableSlotWhenFirstMatchingSlotIsLocked() {
		EntityPlayer player = mock(EntityPlayer.class);
		final InventoryBasic inventory = new InventoryBasic("test", false, 2);
		TestContainer container = new TestContainer(player, 1, 2, normalTargetFactory(), new InventorySlotFactory() {
			@Override
			public Slot create(int index) {
				if (index == 0) {
					return new Slot(inventory, index, 0, 0) {
						@Override
						public boolean canTakeStack(EntityPlayer testPlayer) {
							return false;
						}
					};
				}
				return new Slot(inventory, index, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.PAPER, 1);
		context.setInventory(1, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertTrue(context.inventorySlots.get(1).getStack().isEmpty());
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void lockedRequiredInventoryItemDoesNotMove() {
		EntityPlayer player = mock(EntityPlayer.class);
		final InventoryBasic inventory = new InventoryBasic("test", false, 1);
		TestContainer container = new TestContainer(player, 1, 1, normalTargetFactory(), new InventorySlotFactory() {
			@Override
			public Slot create(int index) {
				return new Slot(inventory, index, 0, 0) {
					@Override
					public boolean canTakeStack(EntityPlayer testPlayer) {
						return false;
					}
				};
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.PAPER, 1);

		transfer(context, recipe(ingredient(Items.PAPER)), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithInvalidSourceSlotId() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 1);
		Map<Integer, Integer> recipeMap = Collections.singletonMap(0, context.container.inventorySlots.size());

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithInvalidAllowedSlotId() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 1);
		List<Integer> craftingSlots = Arrays.asList(
			context.targetSlots.get(0).slotNumber,
			context.container.inventorySlots.size()
		);

		sendPacket(
			context,
			Collections.singletonMap(0, context.inventorySlots.get(0).slotNumber),
			craftingSlots,
			slotIndexes(context.inventorySlots),
			false,
			true
		);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithOversizedSlotList() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 1);
		List<Integer> oversizedCraftingSlots = new ArrayList<>();
		for (int i = 0; i <= context.container.inventorySlots.size(); i++) {
			oversizedCraftingSlots.add(0);
		}

		sendPacket(
			context,
			Collections.singletonMap(0, context.inventorySlots.get(0).slotNumber),
			oversizedCraftingSlots,
			slotIndexes(context.inventorySlots),
			false,
			true
		);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithInvalidCraftingNumber() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.PAPER, 1);

		sendPacket(
			context,
			Collections.singletonMap(1, context.inventorySlots.get(0).slotNumber),
			slotIndexes(context.targetSlots),
			slotIndexes(context.inventorySlots),
			false,
			true
		);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maliciousMaxTransferCannotDuplicateSingleItemAcrossTargets() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.PAPER, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).slotNumber);
		recipeMap.put(1, context.inventorySlots.get(0).slotNumber);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), true, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertTrue(context.targetSlots.get(1).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maliciousPartialMaxTransferCannotDuplicateSingleItemAcrossTargets() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.PAPER, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).slotNumber);
		recipeMap.put(1, context.inventorySlots.get(0).slotNumber);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), true, false);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertTrue(context.targetSlots.get(1).getStack().isEmpty());
		assertTrue(context.inventorySlots.get(0).getStack().isEmpty());
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maxTransferUsingTargetAsSourceDoesNotDuplicateItem() {
		TestContext context = createContext(1, 0);
		context.setTarget(0, Items.PAPER, 1);
		Map<Integer, Integer> recipeMap = Collections.singletonMap(0, context.targetSlots.get(0).slotNumber);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), Collections.<Integer>emptyList(), true, false);

		assertStack(context.targetSlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void rollsBackIncompleteCompleteSet() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.PAPER, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).slotNumber);
		recipeMap.put(1, context.inventorySlots.get(0).slotNumber);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), false, true);

		assertTrue(context.targetSlots.get(0).getStack().isEmpty());
		assertTrue(context.targetSlots.get(1).getStack().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.PAPER, 1);
		assertEquals(1, totalItemCount(context));
	}

	private static TestContext createContext(int targetSlotCount, int inventorySlotCount) {
		EntityPlayer player = mock(EntityPlayer.class);
		return new TestContext(player, new TestContainer(player, targetSlotCount, inventorySlotCount, normalTargetFactory()));
	}

	private static TargetSlotFactory normalTargetFactory() {
		return new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer container, EntityPlayer player, int index) {
				return new Slot(new InventoryBasic("test", false, 1), 0, 0, 0);
			}
		};
	}

	private static StackHelper.MatchingItemsResult transfer(
		TestContext context,
		Map<Integer, TestGuiIngredient> recipe,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
		for (Slot slot : context.container.inventorySlots) {
			if (!slot.getStack().isEmpty()) {
				availableItemStacks.put(slot.slotNumber, slot.getStack().copy());
			}
		}

		StackHelper.MatchingItemsResult result = STACK_HELPER.getMatchingItems(availableItemStacks, recipe);
		if (result.missingItems.isEmpty()) {
			sendPacket(
				context,
				result.matchingItems,
				result.matchingItemCounts,
				slotIndexes(context.targetSlots),
				slotIndexes(context.inventorySlots),
				maxTransfer,
				requireCompleteSets
			);
		}
		return result;
	}

	private static void sendPacket(
		TestContext context,
		Map<Integer, Integer> recipeMap,
		List<Integer> craftingSlots,
		List<Integer> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		Map<Integer, Integer> recipeCountMap = new HashMap<>();
		for (Integer recipeSlot : recipeMap.keySet()) {
			recipeCountMap.put(recipeSlot, 1);
		}
		sendPacket(context, recipeMap, recipeCountMap, craftingSlots, inventorySlots, maxTransfer, requireCompleteSets);
	}

	private static void sendPacket(
		TestContext context,
		Map<Integer, Integer> recipeMap,
		Map<Integer, Integer> recipeCountMap,
		List<Integer> craftingSlots,
		List<Integer> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			recipeMap,
			recipeCountMap,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets
		);
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		packet.writePacketData(buffer);
		PacketRecipeTransfer.readPacketData(buffer, context.player);
	}

	@SafeVarargs
	private static Map<Integer, TestGuiIngredient> recipe(TestGuiIngredient... ingredients) {
		Map<Integer, TestGuiIngredient> recipe = new LinkedHashMap<>();
		for (int i = 0; i < ingredients.length; i++) {
			recipe.put(i, ingredients[i]);
		}
		return recipe;
	}

	private static TestGuiIngredient ingredient(Item... items) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Item item : items) {
			stacks.add(new ItemStack(item));
		}
		return new TestGuiIngredient(stacks);
	}

	private static TestGuiIngredient countedIngredient(int count, Item item) {
		return ingredientStacks(new ItemStack(item, count));
	}

	private static TestGuiIngredient ingredientStacks(ItemStack... stacks) {
		return new TestGuiIngredient(Arrays.asList(stacks));
	}

	private static TestGuiIngredient emptyIngredient() {
		return new TestGuiIngredient(Collections.<ItemStack>emptyList());
	}

	private static List<Integer> slotIndexes(List<Slot> slots) {
		List<Integer> indexes = new ArrayList<>();
		for (Slot slot : slots) {
			indexes.add(slot.slotNumber);
		}
		return indexes;
	}

	private static void assertStack(Slot slot, Item item, int count) {
		assertSame(item, slot.getStack().getItem());
		assertEquals(count, slot.getStack().getCount());
	}

	private static void assertAllOtherSlotsEmpty(TestContext context, Slot... expectedNonEmptySlots) {
		List<Slot> expected = Arrays.asList(expectedNonEmptySlots);
		for (Slot slot : context.container.inventorySlots) {
			if (!expected.contains(slot)) {
				assertTrue("Expected slot " + slot.slotNumber + " to be empty, got " + slot.getStack(), slot.getStack().isEmpty());
			}
		}
	}

	private static int inventoryCount(TestContext context, Item item) {
		int count = 0;
		for (Slot slot : context.inventorySlots) {
			if (slot.getStack().getItem() == item) {
				count += slot.getStack().getCount();
			}
		}
		return count;
	}

	private static int totalItemCount(TestContext context) {
		int count = 0;
		for (Slot slot : context.container.inventorySlots) {
			count += slot.getStack().getCount();
		}
		return count;
	}

	private static final class TestContext {
		private final EntityPlayer player;
		private final TestContainer container;
		private final List<Slot> targetSlots;
		private final List<Slot> inventorySlots;

		private TestContext(EntityPlayer player, TestContainer container) {
			this.player = player;
			this.container = container;
			this.targetSlots = container.targetSlots;
			this.inventorySlots = container.playerInventorySlots;
			player.openContainer = container;
		}

		private void setTarget(int index, Item item, int count) {
			targetSlots.get(index).putStack(new ItemStack(item, count));
		}

		private void setInventory(int index, Item item, int count) {
			inventorySlots.get(index).putStack(new ItemStack(item, count));
		}
	}

	private static final class TestGuiIngredient implements IGuiIngredient<ItemStack> {
		private final List<ItemStack> ingredients;

		private TestGuiIngredient(List<ItemStack> ingredients) {
			this.ingredients = ingredients;
		}

		@Nullable
		@Override
		public ItemStack getDisplayedIngredient() {
			return ingredients.isEmpty() ? null : ingredients.get(0);
		}

		@Override
		public List<ItemStack> getAllIngredients() {
			return ingredients;
		}

		@Override
		public boolean isInput() {
			return true;
		}

		@Override
		public void drawHighlight(net.minecraft.client.Minecraft minecraft, java.awt.Color color, int xOffset, int yOffset) {
		}
	}

	@FunctionalInterface
	private interface TargetSlotFactory {
		Slot create(TestContainer container, EntityPlayer player, int index);
	}

	@FunctionalInterface
	private interface InventorySlotFactory {
		Slot create(int index);
	}

	private static final class TestContainer extends Container {
		private final List<Slot> targetSlots = new ArrayList<>();
		private final List<Slot> playerInventorySlots = new ArrayList<>();

		private TestContainer(EntityPlayer player, int targetSlotCount, int inventorySlotCount, TargetSlotFactory targetSlotFactory) {
			this(player, targetSlotCount, inventorySlotCount, targetSlotFactory, defaultInventorySlotFactory(inventorySlotCount));
		}

		private TestContainer(
			EntityPlayer player,
			int targetSlotCount,
			int inventorySlotCount,
			TargetSlotFactory targetSlotFactory,
			InventorySlotFactory inventorySlotFactory
		) {
			for (int i = 0; i < targetSlotCount; i++) {
				targetSlots.add(addSlotToContainer(targetSlotFactory.create(this, player, i)));
			}
			for (int i = 0; i < inventorySlotCount; i++) {
				playerInventorySlots.add(addSlotToContainer(inventorySlotFactory.create(i)));
			}
		}

		private static InventorySlotFactory defaultInventorySlotFactory(int inventorySlotCount) {
			final InventoryBasic inventory = new InventoryBasic("test", false, inventorySlotCount);
			return new InventorySlotFactory() {
				@Override
				public Slot create(int index) {
					return new Slot(inventory, index, 0, 0);
				}
			};
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return true;
		}
	}
}
