package mezz.jei.test.transfer;

import io.netty.buffer.Unpooled;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.transfer.RecipeTransferUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Bootstrap;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class RecipeTransferIntegrationTest {
	private static final IStackHelper STACK_HELPER = new TestStackHelper();

	@BeforeAll
	public static void bootstrapMinecraft() {
		Bootstrap.bootStrap();
	}

	@Test
	public void transfersRecipeThroughPacketToCraftingGrid() {
		TestContext context = createContext(2, 3);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		context.setInventory(1, Items.STICK, 1);

		RecipeTransferUtil.MatchingItemsResult result = transfer(
			context,
			recipe(ingredient(Items.OAK_PLANKS), ingredient(Items.STICK)),
			false,
			true
		);

		assertTrue(result.missingItems.isEmpty());
		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0), context.targetSlots.get(1));
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void transfersIntoEmptyItemHandlerSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, PlayerEntity testPlayer, int index) {
				return new SlotItemHandler(new ItemStackHandler(1), 0, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		assertFalse(context.targetSlots.get(0).mayPickup(player), "Expected the empty item-handler slot to reject extraction");

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotInsertIntoRejectingItemHandlerSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		final ItemStackHandler rejectingItemHandler = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, PlayerEntity testPlayer, int index) {
				return new SlotItemHandler(rejectingItemHandler, 0, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotInsertIntoRejectingSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, PlayerEntity testPlayer, int index) {
				return new Slot(new Inventory(1), 0, 0, 0) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return false;
					}
				};
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotInsertIntoCraftingResultSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, PlayerEntity testPlayer, int index) {
				return new CraftingResultSlot(
					testPlayer,
					new CraftingInventory(testContainer, 1, 1),
					new CraftResultInventory(),
					0,
					0,
					0
				);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void transfersFromSplitStacks() {
		TestContext context = createContext(3, 2);
		context.setInventory(0, Items.OAK_PLANKS, 2);
		context.setInventory(1, Items.OAK_PLANKS, 1);

		transfer(
			context,
			recipe(ingredient(Items.OAK_PLANKS), ingredient(Items.OAK_PLANKS), ingredient(Items.OAK_PLANKS)),
			false,
			true
		);

		for (Slot targetSlot : context.targetSlots) {
			assertStack(targetSlot, Items.OAK_PLANKS, 1);
		}
		assertAllOtherSlotsEmpty(context, context.targetSlots.toArray(new Slot[0]));
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void transfersAlternativeIngredient() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.BIRCH_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS, Items.BIRCH_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.BIRCH_PLANKS, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
	}

	@Test
	public void transfersAlternativeWithoutStealingRequiredIngredient() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		context.setInventory(1, Items.STICK, 1);

		RecipeTransferUtil.MatchingItemsResult result = transfer(
			context,
			recipe(ingredient(Items.OAK_PLANKS, Items.STICK), ingredient(Items.OAK_PLANKS)),
			false,
			true
		);

		assertTrue(result.missingItems.isEmpty());
		assertStack(context.targetSlots.get(0), Items.STICK, 1);
		assertStack(context.targetSlots.get(1), Items.OAK_PLANKS, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0), context.targetSlots.get(1));
	}

	@Test
	public void reportsMissingIngredientsWithoutChangingMenu() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		RecipeTransferUtil.MatchingItemsResult result = transfer(
			context,
			recipe(ingredient(Items.OAK_PLANKS), ingredient(Items.STICK)),
			false,
			true
		);

		assertEquals(Collections.singletonList(1), result.missingItems);
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertTrue(context.targetSlots.get(1).getItem().isEmpty());
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maxTransferMovesMultipleCompleteSets() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.OAK_PLANKS, 5);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), true, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 5);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(0));
		assertEquals(5, totalItemCount(context));
	}

	@Test
	public void maxTransferStopsAtLimitingIngredient() {
		TestContext context = createContext(2, 2);
		context.setInventory(0, Items.OAK_PLANKS, 5);
		context.setInventory(1, Items.STICK, 2);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS), ingredient(Items.STICK)), true, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 2);
		assertStack(context.targetSlots.get(1), Items.STICK, 2);
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 3);
		assertTrue(context.inventorySlots.get(1).getItem().isEmpty());
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
		context.setInventory(2, Items.OAK_PLANKS, 20);

		transfer(
			context,
			recipe(ingredient(Items.ENDER_PEARL), ingredient(Items.OAK_PLANKS)),
			true,
			true
		);

		assertStack(context.targetSlots.get(0), Items.ENDER_PEARL, 16);
		assertStack(context.targetSlots.get(1), Items.OAK_PLANKS, 16);
		assertEquals(4, inventoryCount(context, Items.ENDER_PEARL));
		assertEquals(4, inventoryCount(context, Items.OAK_PLANKS));
		assertEquals(40, totalItemCount(context));
	}

	@Test
	public void maxTransferStopsAtNonStackableIngredientInMultiIngredientRecipe() {
		TestContext context = createContext(2, 3);
		context.setInventory(0, Items.DIAMOND_SWORD, 1);
		context.setInventory(1, Items.DIAMOND_SWORD, 1);
		context.setInventory(2, Items.OAK_PLANKS, 10);

		transfer(
			context,
			recipe(ingredient(Items.DIAMOND_SWORD), ingredient(Items.OAK_PLANKS)),
			true,
			true
		);

		assertStack(context.targetSlots.get(0), Items.DIAMOND_SWORD, 1);
		assertStack(context.targetSlots.get(1), Items.OAK_PLANKS, 1);
		assertEquals(1, inventoryCount(context, Items.DIAMOND_SWORD));
		assertEquals(9, inventoryCount(context, Items.OAK_PLANKS));
		assertEquals(12, totalItemCount(context));
	}

	@Test
	public void stowsDisplacedCraftingItemInInventory() {
		TestContext context = createContext(2, 3);
		context.setTarget(0, Items.DIRT, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		context.setInventory(1, Items.STICK, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS), ingredient(Items.STICK)), false, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertEquals(1, inventoryCount(context, Items.DIRT));
		assertEquals(3, totalItemCount(context));
	}

	@Test
	public void stowsDisplacedCraftingItemIntoMatchingInventoryStack() {
		TestContext context = createContext(1, 2);
		context.setTarget(0, Items.DIRT, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		context.setInventory(1, Items.DIRT, 63);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertTrue(context.inventorySlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(1), Items.DIRT, 64);
		assertEquals(65, totalItemCount(context));
	}

	@Test
	public void doesNotStowDisplacedItemIntoRejectingInventorySlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		final Inventory inventory = new Inventory(2);
		TestContainer container = new TestContainer(player, 1, 2, normalTargetFactory(), new InventorySlotFactory() {
			@Override
			public Slot create(int index) {
				if (index == 0) {
					return new Slot(inventory, index, 0, 0) {
						@Override
						public boolean mayPlace(ItemStack stack) {
							return stack.getItem() != Items.DIRT;
						}
					};
				}
				return new Slot(inventory, index, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setTarget(0, Items.DIRT, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertTrue(context.inventorySlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(1), Items.DIRT, 1);
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void transfersIngredientAlreadyInTargetSlot() {
		TestContext context = createContext(1, 1);
		context.setTarget(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertTrue(context.inventorySlots.get(0).getItem().isEmpty());
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void doesNotOverwriteLockedOccupiedTargetSlot() {
		PlayerEntity player = mock(PlayerEntity.class);
		TestContainer container = new TestContainer(player, 1, 1, new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer testContainer, PlayerEntity testPlayer, int index) {
				return new Slot(new Inventory(1), 0, 0, 0) {
					@Override
					public boolean mayPickup(PlayerEntity player) {
						return false;
					}
				};
			}
		});
		TestContext context = new TestContext(player, container);
		context.setTarget(0, Items.DIRT, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.DIRT, 1);
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void movesIngredientFromWrongCraftingSlot() {
		TestContext context = createContext(2, 1);
		context.setTarget(0, Items.STICK, 1);

		transfer(context, recipe(emptyIngredient(), ingredient(Items.STICK)), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.targetSlots.get(1), Items.STICK, 1);
		assertAllOtherSlotsEmpty(context, context.targetSlots.get(1));
	}

	@Test
	public void transfersFromMovableSlotWhenFirstMatchingSlotIsLocked() {
		PlayerEntity player = mock(PlayerEntity.class);
		final Inventory inventory = new Inventory(2);
		TestContainer container = new TestContainer(player, 1, 2, normalTargetFactory(), new InventorySlotFactory() {
			@Override
			public Slot create(int index) {
				if (index == 0) {
					return new Slot(inventory, index, 0, 0) {
						@Override
						public boolean mayPickup(PlayerEntity testPlayer) {
							return false;
						}
					};
				}
				return new Slot(inventory, index, 0, 0);
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		context.setInventory(1, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertTrue(context.inventorySlots.get(1).getItem().isEmpty());
		assertEquals(2, totalItemCount(context));
	}

	@Test
	public void lockedRequiredInventoryItemDoesNotMove() {
		PlayerEntity player = mock(PlayerEntity.class);
		final Inventory inventory = new Inventory(1);
		TestContainer container = new TestContainer(player, 1, 1, normalTargetFactory(), new InventorySlotFactory() {
			@Override
			public Slot create(int index) {
				return new Slot(inventory, index, 0, 0) {
					@Override
					public boolean mayPickup(PlayerEntity testPlayer) {
						return false;
					}
				};
			}
		});
		TestContext context = new TestContext(player, container);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		transfer(context, recipe(ingredient(Items.OAK_PLANKS)), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithInvalidSourceSlotId() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		Map<Integer, Integer> recipeMap = Collections.singletonMap(0, context.container.slots.size());

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithInvalidAllowedSlotId() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		List<Integer> craftingSlots = Arrays.asList(
			context.targetSlots.get(0).index,
			context.container.slots.size()
		);

		sendPacket(
			context,
			Collections.singletonMap(0, context.inventorySlots.get(0).index),
			craftingSlots,
			slotIndexes(context.inventorySlots),
			false,
			true
		);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithOversizedSlotList() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		List<Integer> oversizedCraftingSlots = new ArrayList<>();
		for (int i = 0; i <= context.container.slots.size(); i++) {
			oversizedCraftingSlots.add(0);
		}

		sendPacket(
			context,
			Collections.singletonMap(0, context.inventorySlots.get(0).index),
			oversizedCraftingSlots,
			slotIndexes(context.inventorySlots),
			false,
			true
		);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void ignoresPacketWithInvalidCraftingNumber() {
		TestContext context = createContext(1, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);

		sendPacket(
			context,
			Collections.singletonMap(1, context.inventorySlots.get(0).index),
			slotIndexes(context.targetSlots),
			slotIndexes(context.inventorySlots),
			false,
			true
		);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maliciousMaxTransferCannotDuplicateSingleItemAcrossTargets() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).index);
		recipeMap.put(1, context.inventorySlots.get(0).index);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), true, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertTrue(context.targetSlots.get(1).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maliciousPartialMaxTransferCannotDuplicateSingleItemAcrossTargets() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).index);
		recipeMap.put(1, context.inventorySlots.get(0).index);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), true, false);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertTrue(context.targetSlots.get(1).getItem().isEmpty());
		assertTrue(context.inventorySlots.get(0).getItem().isEmpty());
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void maxTransferUsingTargetAsSourceDoesNotDuplicateItem() {
		TestContext context = createContext(1, 0);
		context.setTarget(0, Items.OAK_PLANKS, 1);
		Map<Integer, Integer> recipeMap = Collections.singletonMap(0, context.targetSlots.get(0).index);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), Collections.<Integer>emptyList(), true, false);

		assertStack(context.targetSlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	@Test
	public void rollsBackIncompleteCompleteSet() {
		TestContext context = createContext(2, 1);
		context.setInventory(0, Items.OAK_PLANKS, 1);
		Map<Integer, Integer> recipeMap = new LinkedHashMap<>();
		recipeMap.put(0, context.inventorySlots.get(0).index);
		recipeMap.put(1, context.inventorySlots.get(0).index);

		sendPacket(context, recipeMap, slotIndexes(context.targetSlots), slotIndexes(context.inventorySlots), false, true);

		assertTrue(context.targetSlots.get(0).getItem().isEmpty());
		assertTrue(context.targetSlots.get(1).getItem().isEmpty());
		assertStack(context.inventorySlots.get(0), Items.OAK_PLANKS, 1);
		assertEquals(1, totalItemCount(context));
	}

	private static TestContext createContext(int targetSlotCount, int inventorySlotCount) {
		PlayerEntity player = mock(PlayerEntity.class);
		return new TestContext(player, new TestContainer(player, targetSlotCount, inventorySlotCount, normalTargetFactory()));
	}

	private static TargetSlotFactory normalTargetFactory() {
		return new TargetSlotFactory() {
			@Override
			public Slot create(TestContainer container, PlayerEntity player, int index) {
				return new Slot(new Inventory(1), 0, 0, 0);
			}
		};
	}

	private static RecipeTransferUtil.MatchingItemsResult transfer(
		TestContext context,
		Map<Integer, TestGuiIngredient> recipe,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
		for (Slot slot : context.container.slots) {
			if (slot.hasItem()) {
				availableItemStacks.put(slot.index, slot.getItem().copy());
			}
		}

		RecipeTransferUtil.MatchingItemsResult result = RecipeTransferUtil.getMatchingItems(
			STACK_HELPER,
			availableItemStacks,
			recipe
		);
		if (result.missingItems.isEmpty()) {
			sendPacket(
				context,
				result.matchingItems,
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
		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			recipeMap,
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

	private static TestGuiIngredient emptyIngredient() {
		return new TestGuiIngredient(Collections.<ItemStack>emptyList());
	}

	private static List<Integer> slotIndexes(List<Slot> slots) {
		List<Integer> indexes = new ArrayList<>();
		for (Slot slot : slots) {
			indexes.add(slot.index);
		}
		return indexes;
	}

	private static void assertStack(Slot slot, Item item, int count) {
		assertSame(item, slot.getItem().getItem());
		assertEquals(count, slot.getItem().getCount());
	}

	private static void assertAllOtherSlotsEmpty(TestContext context, Slot... expectedNonEmptySlots) {
		List<Slot> expected = Arrays.asList(expectedNonEmptySlots);
		for (Slot slot : context.container.slots) {
			if (!expected.contains(slot)) {
				assertTrue(slot.getItem().isEmpty(), "Expected slot " + slot.index + " to be empty, got " + slot.getItem());
			}
		}
	}

	private static int inventoryCount(TestContext context, Item item) {
		int count = 0;
		for (Slot slot : context.inventorySlots) {
			if (slot.getItem().getItem() == item) {
				count += slot.getItem().getCount();
			}
		}
		return count;
	}

	private static int totalItemCount(TestContext context) {
		int count = 0;
		for (Slot slot : context.container.slots) {
			count += slot.getItem().getCount();
		}
		return count;
	}

	private static final class TestContext {
		private final PlayerEntity player;
		private final TestContainer container;
		private final List<Slot> targetSlots;
		private final List<Slot> inventorySlots;

		private TestContext(PlayerEntity player, TestContainer container) {
			this.player = player;
			this.container = container;
			this.targetSlots = container.targetSlots;
			this.inventorySlots = container.inventorySlots;
			player.containerMenu = container;
		}

		private void setTarget(int index, Item item, int count) {
			targetSlots.get(index).set(new ItemStack(item, count));
		}

		private void setInventory(int index, Item item, int count) {
			inventorySlots.get(index).set(new ItemStack(item, count));
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
		public void drawHighlight(com.mojang.blaze3d.matrix.MatrixStack matrixStack, int color, int xOffset, int yOffset) {
		}
	}

	private static final class TestStackHelper implements IStackHelper {
		@Override
		public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, UidContext context) {
			return lhs != null && rhs != null && ItemStack.isSame(lhs, rhs) && ItemStack.tagMatches(lhs, rhs);
		}

		@Override
		public String getUniqueIdentifierForStack(ItemStack stack, UidContext context) {
			return stack.getItem().getRegistryName() + ":" + String.valueOf(stack.getTag());
		}
	}

	@FunctionalInterface
	private interface TargetSlotFactory {
		Slot create(TestContainer container, PlayerEntity player, int index);
	}

	@FunctionalInterface
	private interface InventorySlotFactory {
		Slot create(int index);
	}

	private static final class TestContainer extends Container {
		private final List<Slot> targetSlots = new ArrayList<>();
		private final List<Slot> inventorySlots = new ArrayList<>();

		private TestContainer(PlayerEntity player, int targetSlotCount, int inventorySlotCount, TargetSlotFactory targetSlotFactory) {
			this(player, targetSlotCount, inventorySlotCount, targetSlotFactory, defaultInventorySlotFactory(inventorySlotCount));
		}

		private TestContainer(
			PlayerEntity player,
			int targetSlotCount,
			int inventorySlotCount,
			TargetSlotFactory targetSlotFactory,
			InventorySlotFactory inventorySlotFactory
		) {
			super(null, 0);
			for (int i = 0; i < targetSlotCount; i++) {
				targetSlots.add(addSlot(targetSlotFactory.create(this, player, i)));
			}
			for (int i = 0; i < inventorySlotCount; i++) {
				inventorySlots.add(addSlot(inventorySlotFactory.create(i)));
			}
		}

		private static InventorySlotFactory defaultInventorySlotFactory(int inventorySlotCount) {
			final Inventory inventory = new Inventory(inventorySlotCount);
			return new InventorySlotFactory() {
				@Override
				public Slot create(int index) {
					return new Slot(inventory, index, 0, 0);
				}
			};
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
