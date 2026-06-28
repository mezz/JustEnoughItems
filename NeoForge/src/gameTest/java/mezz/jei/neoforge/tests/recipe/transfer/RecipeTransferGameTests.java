package mezz.jei.neoforge.tests.recipe.transfer;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import mezz.jei.neoforge.tests.lib.TestRecipeSlotView;
import mezz.jei.neoforge.tests.lib.TransferRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ForEachTest(groups = "recipe_transfer")
public final class RecipeTransferGameTests {
	private RecipeTransferGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers recipe ingredients into a crafting table.")
	public static void transfersToCraftingTable(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = craftingTableRecipe();
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS, 4)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers recipe ingredients into the player's 2x2 crafting grid.")
	public static void transfersToPlayerCraftingGrid(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = craftingTableRecipe();
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS, 4)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, (containerId, inventory) -> inventory.player.inventoryMenu, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers recipe ingredients into a crafter.")
	public static void transfersToCrafter(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = craftingTableRecipe();
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS, 4)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CrafterMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getCrafterSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a smelting input into a furnace.")
	public static void transfersToFurnaceInput(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("smelting_input", Items.RAW_IRON);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.RAW_IRON)};
		var result = helper.transferFromInventory(RecipeTypes.SMELTING, recipe, FurnaceMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getFurnaceIngredientSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers smelting fuel into a furnace.")
	public static void transfersToFurnaceFuel(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("smelting_fuel", Items.COAL);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.COAL)};
		var result = helper.transferFromInventory(RecipeTypes.SMELTING_FUEL, recipe, FurnaceMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getFurnaceFuelSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a smoking input into a smoker.")
	public static void transfersToSmokerInput(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("smoking_input", Items.PORKCHOP);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.PORKCHOP)};
		var result = helper.transferFromInventory(RecipeTypes.SMOKING, recipe, SmokerMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getFurnaceIngredientSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers smoking fuel into a smoker.")
	public static void transfersToSmokerFuel(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("smoking_fuel", Items.COAL);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.COAL)};
		var result = helper.transferFromInventory(RecipeTypes.SMOKING_FUEL, recipe, SmokerMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getFurnaceFuelSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a blasting input into a blast furnace.")
	public static void transfersToBlastFurnaceInput(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("blasting_input", Items.RAW_IRON);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.RAW_IRON)};
		var result = helper.transferFromInventory(RecipeTypes.BLASTING, recipe, BlastFurnaceMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getFurnaceIngredientSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers blasting fuel into a blast furnace.")
	public static void transfersToBlastFurnaceFuel(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("blasting_fuel", Items.COAL);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.COAL)};
		var result = helper.transferFromInventory(RecipeTypes.BLASTING_FUEL, recipe, BlastFurnaceMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getFurnaceFuelSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers bottles and an ingredient into a brewing stand.")
	public static void transfersToBrewingStand(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("brewing", Items.GLASS_BOTTLE, Items.GLASS_BOTTLE, Items.GLASS_BOTTLE, Items.NETHER_WART);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.GLASS_BOTTLE, 3), stack(Items.NETHER_WART)};
		var result = helper.transferFromInventory(RecipeTypes.BREWING, recipe, BrewingStandMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getBrewingRecipeSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers inputs into an anvil.")
	public static void transfersToAnvil(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("anvil", Items.IRON_SWORD, Items.IRON_INGOT);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.IRON_SWORD), stack(Items.IRON_INGOT)};
		var result = helper.transferFromInventory(RecipeTypes.ANVIL, recipe, AnvilMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getAnvilSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers inputs into a smithing table.")
	public static void transfersToSmithingTable(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = basicRecipe("smithing", Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.DIAMOND_SWORD, Items.NETHERITE_INGOT);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), stack(Items.DIAMOND_SWORD), stack(Items.NETHERITE_INGOT)};
		var result = helper.transferFromInventory(RecipeTypes.SMITHING, recipe, SmithingMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getSmithingSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a recipe from split inventory stacks.")
	public static void transfersFromSplitStacks(RecipeTransferTestHelper helper) {
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe("two_planks", stack(Items.STICK), grid(
			ingredient(0, Items.OAK_PLANKS),
			ingredient(1, Items.OAK_PLANKS)
		));
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.STICK), stack(Items.OAK_PLANKS), stack(Items.OAK_PLANKS)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers an available alternative ingredient.")
	public static void transfersAlternativeIngredient(RecipeTransferTestHelper helper) {
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe("alternative_plank", stack(Items.STICK), grid(
			ingredient(0, TestRecipeSlotView.items(stack(Items.OAK_PLANKS), stack(Items.BIRCH_PLANKS)))
		));
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.BIRCH_PLANKS)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers the matching item subtype instead of the same item with different components.")
	public static void transfersExactSubtypeIngredient(RecipeTransferTestHelper helper) {
		ItemStack waterPotion = lingeringPotion(Potions.WATER);
		ItemStack healingPotion = lingeringPotion(Potions.HEALING);
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe("tipped_arrow", stack(Items.TIPPED_ARROW, 8), grid(
			ingredient(0, Items.ARROW),
			ingredient(1, Items.ARROW),
			ingredient(2, Items.ARROW),
			ingredient(3, Items.ARROW),
			ingredient(4, TestRecipeSlotView.item(waterPotion)),
			ingredient(5, Items.ARROW),
			ingredient(6, Items.ARROW),
			ingredient(7, Items.ARROW),
			ingredient(8, Items.ARROW)
		));
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.ARROW, 8), healingPotion, waterPotion};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, false, inventoryStacks);
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.assertSlot(result.menu().getInputGridSlots().get(4), waterPotion);
		helper.assertSlot(result.sourceSlots().get(1).slot(), healingPotion);
		helper.assertEmptySlot(result.sourceSlots().get(2).slot());
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports a user-facing error when recipe ingredients are missing.")
	public static void reportsMissingIngredients(RecipeTransferTestHelper helper) {
		TransferRecipe<?> recipe = craftingTableRecipe();
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS, 3)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, false, inventoryStacks);
		helper.assertFailedTransfer(result, helper::getCraftingGridSlots, RecipeTransferErrorMissingSlots.class);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer moves as many complete recipe sets as possible.")
	public static void maxTransferMovesMultipleCompleteSets(RecipeTransferTestHelper helper) {
		TransferRecipe<TestRecipe> recipe = basicRecipe("max_transfer_sets", Items.OAK_PLANKS, Items.OAK_PLANKS);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS, 10)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, true, inventoryStacks);
		helper.assertTransferSucceeded(result);

		List<Slot> craftingSlots = result.menu().getInputGridSlots();
		helper.assertSlot(craftingSlots.get(0), Items.OAK_PLANKS, 5);
		helper.assertSlot(craftingSlots.get(1), Items.OAK_PLANKS, 5);
		helper.assertEmptySlot(result.sourceSlots().getFirst().slot());
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer stops at the limiting ingredient count.")
	public static void maxTransferStopsAtLimitingIngredient(RecipeTransferTestHelper helper) {
		TransferRecipe<TestRecipe> recipe = basicRecipe("max_transfer_limited", Items.OAK_PLANKS, Items.STICK);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS, 10), stack(Items.STICK, 3)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, CraftingMenu::new, true, inventoryStacks);
		helper.assertTransferSucceeded(result);

		List<Slot> craftingSlots = result.menu().getInputGridSlots();
		helper.assertSlot(craftingSlots.get(0), Items.OAK_PLANKS, 3);
		helper.assertSlot(craftingSlots.get(1), Items.STICK, 3);
		helper.assertSlot(result.sourceSlots().get(0).slot(), Items.OAK_PLANKS, 7);
		helper.assertEmptySlot(result.sourceSlots().get(1).slot());
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer respects non-stackable recipe slots.")
	public static void maxTransferStopsAtNonStackableSlotLimit(RecipeTransferTestHelper helper) {
		TransferRecipe<TestRecipe> recipe = basicRecipe("max_transfer_non_stackable", Items.IRON_SWORD);
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.IRON_SWORD), stack(Items.IRON_SWORD)};
		var result = helper.transferFromInventory(RecipeTypes.ANVIL, recipe, AnvilMenu::new, true, inventoryStacks);
		helper.assertTransferSucceeded(result);

		helper.assertSlot(result.menu().getSlot(AnvilMenu.INPUT_SLOT), Items.IRON_SWORD, 1);
		helper.assertEmptySlot(result.sourceSlots().get(0).slot());
		helper.assertSlot(result.sourceSlots().get(1).slot(), Items.IRON_SWORD, 1);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers into a full crafting grid when a full inventory can stow the displaced items.")
	public static void transfersWithFullInventoryAndFullCraftingGrid(RecipeTransferTestHelper helper) {
		CraftingMenu menu = helper.createMenu(CraftingMenu::new);
		fillSlots(helper.getStandardInventorySlots(menu), stack(Items.DIRT, 63));
		helper.getStandardInventorySlots(menu).getFirst().set(stack(Items.OAK_PLANKS, 9));
		fillSlots(menu.getInputGridSlots(), stack(Items.DIRT));
		int expectedInventoryDirt = countItem(helper.getStandardInventorySlots(menu), Items.DIRT) + menu.getInputGridSlots().size();

		TransferRecipe<TestRecipe> recipe = basicRecipe("full_inventory_full_grid_success",
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS,
			Items.OAK_PLANKS
		);
		var result = helper.transferFromMenu(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);
		helper.assertTransferSucceeded(result);

		for (Slot craftingSlot : menu.getInputGridSlots()) {
			helper.assertSlot(craftingSlot, Items.OAK_PLANKS, 1);
		}
		int actualInventoryDirt = countItem(helper.getStandardInventorySlots(menu), Items.DIRT);
		if (actualInventoryDirt != expectedInventoryDirt) {
			throw helper.createFailException("Expected displaced crafting items to be stowed into the inventory");
		}
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports inventory full when a full crafting grid cannot be cleared into a full inventory.")
	public static void reportsInventoryFullWithFullInventoryAndFullCraftingGrid(RecipeTransferTestHelper helper) {
		CraftingMenu menu = helper.createMenu(CraftingMenu::new);
		fillSlots(helper.getStandardInventorySlots(menu), stack(Items.DIRT, 64));
		helper.getStandardInventorySlots(menu).getFirst().set(stack(Items.OAK_PLANKS));
		fillSlots(menu.getInputGridSlots(), stack(Items.DIRT));

		TransferRecipe<TestRecipe> recipe = basicRecipe("full_inventory_full_grid_failure", Items.OAK_PLANKS);
		var result = helper.transferFromMenu(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);
		helper.assertTransferError(result, RecipeTransferErrorTooltip.class);

		for (Slot craftingSlot : menu.getInputGridSlots()) {
			helper.assertSlot(craftingSlot, Items.DIRT, 1);
		}
		helper.assertSlot(helper.getStandardInventorySlots(menu).getFirst(), Items.OAK_PLANKS, 1);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers using an ingredient that is already in the crafting grid.")
	public static void transfersFromOccupiedCraftingGrid(RecipeTransferTestHelper helper) {
		CraftingMenu menu = helper.createMenu(CraftingMenu::new);
		Slot ingredientSlot = menu.getInputGridSlots().getFirst();
		ingredientSlot.set(stack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = basicRecipe("already_in_grid", Items.OAK_PLANKS);
		var result = helper.transferFromMenu(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);
		helper.assertTransferSucceeded(result);
		helper.assertSlot(ingredientSlot, Items.OAK_PLANKS, 1);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers an ingredient from the wrong crafting grid slot.")
	public static void transfersFromWrongCraftingGridSlot(RecipeTransferTestHelper helper) {
		CraftingMenu menu = helper.createMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		craftingSlots.get(0).set(stack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = new TransferRecipe<>(
			new TestRecipe("wrong_grid_slot"),
			List.of(
				TestRecipeSlotView.empty(),
				TestRecipeSlotView.item(Items.OAK_PLANKS)
			)
		);
		var result = helper.transferFromMenu(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);
		helper.assertTransferSucceeded(result);
		helper.assertEmptySlot(craftingSlots.get(0));
		helper.assertSlot(craftingSlots.get(1), Items.OAK_PLANKS, 1);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Stows displaced crafting grid items into matching inventory stacks.")
	public static void stowsDisplacedCraftingGridItemsIntoInventoryStacks(RecipeTransferTestHelper helper) {
		CraftingMenu menu = helper.createMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		craftingSlots.get(0).set(stack(Items.DIRT));
		inventorySlots.get(0).set(stack(Items.OAK_PLANKS));
		inventorySlots.get(1).set(stack(Items.DIRT, 63));

		TransferRecipe<TestRecipe> recipe = basicRecipe("stow_displaced_grid_item", Items.OAK_PLANKS);
		var result = helper.transferFromMenu(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);
		helper.assertTransferSucceeded(result);
		helper.assertSlot(craftingSlots.get(0), Items.OAK_PLANKS, 1);
		helper.assertEmptySlot(inventorySlots.get(0));
		helper.assertSlot(inventorySlots.get(1), Items.DIRT, 64);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Rejects crafting recipes that do not fit in the player's 2x2 grid.")
	public static void rejectsTooLargePlayerCraftingRecipe(RecipeTransferTestHelper helper) {
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe("too_large_player_inventory", stack(Items.STICK), grid(
			ingredient(8, Items.OAK_PLANKS)
		));
		ItemStack[] inventoryStacks = new ItemStack[]{stack(Items.OAK_PLANKS)};
		var result = helper.transferFromInventory(RecipeTypes.CRAFTING, recipe, (containerId, inventory) -> inventory.player.inventoryMenu, false, inventoryStacks);
		helper.assertRecipeHasIngredientsOutsidePlayerGrid(result);
		helper.assertFailedTransfer(result, helper::getCraftingGridSlots, RecipeTransferErrorTooltip.class);
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Rolls back partial extra transfers when complete sets are required.")
	public static void rollsBackIncompleteCompleteSet(RecipeTransferTestHelper helper) {
		ServerPlayer player = helper.getPlayer();
		CraftingMenu menu = helper.createMenu(CraftingMenu::new);

		Slot planksSlot = helper.getStandardInventorySlots(menu).get(0);
		Slot sticksSlot = helper.getStandardInventorySlots(menu).get(1);
		planksSlot.set(new ItemStack(Items.OAK_PLANKS, 2));
		sticksSlot.set(new ItemStack(Items.STICK, 1));

		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		List<TransferOperation> operations = List.of(
			new TransferOperation(planksSlot.index, craftingSlots.get(0).index),
			new TransferOperation(sticksSlot.index, craftingSlots.get(1).index)
		);

		BasicRecipeTransferHandlerServer.setItems(
			player,
			operations,
			craftingSlots,
			inventorySlots,
			true,
			true
		);

		helper.assertSlot(craftingSlots.get(0), Items.OAK_PLANKS, 1);
		helper.assertSlot(craftingSlots.get(1), Items.STICK, 1);
		helper.assertSlot(planksSlot, Items.OAK_PLANKS, 1);
		helper.assertEmptySlot(sticksSlot);
		helper.succeed();
	}

	private static TransferRecipe<RecipeHolder<CraftingRecipe>> craftingTableRecipe() {
		return craftingRecipe("crafting_table", stack(Items.CRAFTING_TABLE), grid(
			ingredient(0, Items.OAK_PLANKS),
			ingredient(1, Items.OAK_PLANKS),
			ingredient(3, Items.OAK_PLANKS),
			ingredient(4, Items.OAK_PLANKS)
		));
	}

	private static TransferRecipe<RecipeHolder<CraftingRecipe>> craftingRecipe(String idPath, ItemStack result, List<TestRecipeSlotView> inputSlots) {
		Identifier id = Identifier.fromNamespaceAndPath("jeitests", "recipe_transfer/" + idPath);
		ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, id);
		List<Optional<Ingredient>> ingredients = inputSlots.stream()
			.map(slot -> slot.isEmpty() ? Optional.<Ingredient>empty() : Optional.of(slot.ingredient()))
			.toList();
		CraftingRecipe recipe = new ShapedRecipe(
			new Recipe.CommonInfo(false),
			new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
			new ShapedRecipePattern(3, 3, ingredients, Optional.empty()),
			ItemStackTemplate.fromNonEmptyStack(result)
		);
		return new TransferRecipe<>(new RecipeHolder<>(resourceKey, recipe), inputSlots);
	}

	private static TransferRecipe<TestRecipe> basicRecipe(String id, Item... items) {
		return new TransferRecipe<>(
			new TestRecipe(id),
			Stream.of(items)
				.map(TestRecipeSlotView::item)
				.toList()
		);
	}

	private static void fillSlots(List<Slot> slots, ItemStack stack) {
		for (Slot slot : slots) {
			slot.set(stack.copy());
		}
	}

	private static int countItem(List<Slot> slots, Item item) {
		return slots.stream()
			.map(Slot::getItem)
			.filter(stack -> stack.is(item))
			.mapToInt(ItemStack::getCount)
			.sum();
	}

	private static List<TestRecipeSlotView> grid(RecipeSlotPlacement... placements) {
		List<TestRecipeSlotView> slots = emptyGrid();
		for (RecipeSlotPlacement placement : placements) {
			slots.set(placement.index, placement.slot);
		}
		return slots;
	}

	private static List<TestRecipeSlotView> emptyGrid() {
		List<TestRecipeSlotView> slots = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			slots.add(TestRecipeSlotView.empty());
		}
		return slots;
	}

	private static RecipeSlotPlacement ingredient(int index, Item item) {
		return ingredient(index, TestRecipeSlotView.item(item));
	}

	private static RecipeSlotPlacement ingredient(int index, TestRecipeSlotView slot) {
		return new RecipeSlotPlacement(index, slot);
	}

	private static ItemStack stack(Item item) {
		return stack(item, 1);
	}

	private static ItemStack stack(Item item, int count) {
		return new ItemStack(item, count);
	}

	private static ItemStack lingeringPotion(Holder<Potion> potion) {
		ItemStack stack = new ItemStack(Items.LINGERING_POTION);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}

	private record TestRecipe(String id) {
	}

	private record RecipeSlotPlacement(int index, TestRecipeSlotView slot) {
	}
}
