package mezz.jei.forge.tests.recipe.transfer;

import static mezz.jei.forge.tests.lib.TestRecipes.basicRecipe;
import static mezz.jei.forge.tests.lib.TestRecipes.craftingRecipe;
import static mezz.jei.forge.tests.lib.TestRecipes.grid;
import static mezz.jei.forge.tests.lib.TestRecipes.ingredient;
import static mezz.jei.forge.tests.lib.TestRecipes.testRecipe;
import static mezz.jei.forge.tests.lib.StackPlacement.stackAt;

import io.netty.buffer.Unpooled;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.TransferOperation;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import mezz.jei.forge.tests.lib.JeiGameTestHelper;
import mezz.jei.forge.tests.lib.StackPlacement;
import mezz.jei.forge.tests.lib.TestRecipes.TestRecipe;
import mezz.jei.forge.tests.lib.TestRecipeSlotView;
import mezz.jei.forge.tests.lib.TransferRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder("jei")
@PrefixGameTestTemplate(false)
public final class RecipeTransferGameTests {
	private static final int CRAFTING_GRID_TOP_LEFT = 0;
	private static final int CRAFTING_GRID_TOP_CENTER = 1;
	private static final int CRAFTING_GRID_TOP_RIGHT = 2;
	private static final int CRAFTING_GRID_MIDDLE_LEFT = 3;
	private static final int CRAFTING_GRID_CENTER = 4;
	private static final int CRAFTING_GRID_MIDDLE_RIGHT = 5;
	private static final int CRAFTING_GRID_BOTTOM_LEFT = 6;
	private static final int CRAFTING_GRID_BOTTOM_CENTER = 7;
	private static final int CRAFTING_GRID_BOTTOM_RIGHT = 8;
	private static final int PLAYER_CRAFTING_TOP_LEFT = 0;
	private static final int PLAYER_CRAFTING_TOP_RIGHT = 1;
	private static final int PLAYER_CRAFTING_BOTTOM_LEFT = 2;
	private static final int PLAYER_CRAFTING_BOTTOM_RIGHT = 3;
	private static final int BREWING_LEFT_BOTTLE_SLOT = 0;
	private static final int BREWING_CENTER_BOTTLE_SLOT = 1;
	private static final int BREWING_RIGHT_BOTTLE_SLOT = 2;
	private static final int BREWING_INGREDIENT_SLOT = 3;
	private static final int BREWING_FUEL_SLOT = 4;

	private RecipeTransferGameTests() {
	}

	@GameTest(template = "empty")
	public static void transfersToCraftingTable(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: a crafting table recipe has all four planks available in inventory.
		TransferRecipe<?> recipe = craftingTableRecipe();
		var menu = helper.openMenu(RecipeTransferGameTests::createCraftingMenu, new ItemStack(Items.OAK_PLANKS, 4));

		// Operation: transfer the recipe into a vanilla crafting table menu.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: each recipe slot is filled and inventory changes reconcile with the moved planks.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of(stackAt(0, Items.CRAFTING_TABLE))
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS),
					stackAt(CRAFTING_GRID_MIDDLE_LEFT, Items.OAK_PLANKS),
					stackAt(CRAFTING_GRID_CENTER, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	@SuppressWarnings("removal")
	public static void transfersIntoEmptyItemHandlerSlot(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the target is a real NeoForge item-handler slot whose empty contents cannot be modified.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot targetSlot = replaceSlot(
			menu,
			craftingSlots.get(CRAFTING_GRID_TOP_LEFT),
			itemHandlerSlot(craftingSlots.get(CRAFTING_GRID_TOP_LEFT), new ItemStackHandler(1))
		);
		helper.getStandardInventorySlots(menu).get(0).set(new ItemStack(Items.OAK_PLANKS));
		helper.assertTrue(!targetSlot.allowModification(helper.getPlayer()), "Expected the empty item-handler slot to reject content modification");

		TransferRecipe<TestRecipe> recipe = basicRecipe("empty_item_handler_slot", Items.OAK_PLANKS);
		// Operation: transfer through the normal client and server recipe-transfer path.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu);

		// Assertions: the ingredient moves from inventory into the item handler.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS))
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToPlayerCraftingGrid(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: a 2x2 crafting recipe has all four planks available in inventory.
		TransferRecipe<?> recipe = craftingTableRecipe();
		var menu = helper.openMenu(
			(containerId, inventory) -> inventory.player.inventoryMenu,
			new ItemStack(Items.OAK_PLANKS, 4)
		);

		// Operation: transfer the recipe into the player's inventory crafting grid.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the player crafting grid receives the recipe ingredients.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of(stackAt(0, Items.CRAFTING_TABLE))
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getPlayerCraftingInputSlots,
				List.of(
					stackAt(PLAYER_CRAFTING_TOP_LEFT, Items.OAK_PLANKS),
					stackAt(PLAYER_CRAFTING_TOP_RIGHT, Items.OAK_PLANKS),
					stackAt(PLAYER_CRAFTING_BOTTOM_LEFT, Items.OAK_PLANKS),
					stackAt(PLAYER_CRAFTING_BOTTOM_RIGHT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				RecipeTransferGameTests::getPlayerInventorySlots,
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToFurnaceInput(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: one raw iron stack is available as a smelting ingredient.
		TransferRecipe<?> recipe = basicRecipe("smelting_input", Items.RAW_IRON);
		var menu = helper.openMenu(FurnaceMenu::new, new ItemStack(Items.RAW_IRON));

		// Operation: transfer the smelting ingredient into a furnace.
		var result = helper.transfer(RecipeTypes.SMELTING, recipe, menu, false);

		// Assertions: the furnace input slot receives the raw iron.
		helper.assertSuccessfulTransfer(result, helper::getFurnaceIngredientSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getFurnaceResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getFurnaceCraftAreaSlots,
				List.of(
					stackAt(AbstractFurnaceMenu.INGREDIENT_SLOT, Items.RAW_IRON)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToFurnaceFuel(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: one coal stack is available as furnace fuel.
		TransferRecipe<?> recipe = basicRecipe("smelting_fuel", Items.COAL);
		var menu = helper.openMenu(FurnaceMenu::new, new ItemStack(Items.COAL));

		// Operation: transfer the fuel ingredient into a furnace.
		var result = helper.transfer(RecipeTypes.FUELING, recipe, menu, false);

		// Assertions: the furnace fuel slot receives the coal.
		helper.assertSuccessfulTransfer(result, helper::getFurnaceFuelSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getFurnaceResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getFurnaceCraftAreaSlots,
				List.of(
					stackAt(AbstractFurnaceMenu.FUEL_SLOT, Items.COAL)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToSmokerInput(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: one porkchop stack is available as a smoking ingredient.
		TransferRecipe<?> recipe = basicRecipe("smoking_input", Items.PORKCHOP);
		var menu = helper.openMenu(SmokerMenu::new, new ItemStack(Items.PORKCHOP));

		// Operation: transfer the smoking ingredient into a smoker.
		var result = helper.transfer(RecipeTypes.SMOKING, recipe, menu, false);

		// Assertions: the smoker input slot receives the porkchop.
		helper.assertSuccessfulTransfer(result, helper::getFurnaceIngredientSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getFurnaceResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getFurnaceCraftAreaSlots,
				List.of(
					stackAt(AbstractFurnaceMenu.INGREDIENT_SLOT, Items.PORKCHOP)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToSmokerFuel(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: one coal stack is available as smoker fuel.
		TransferRecipe<?> recipe = basicRecipe("smoking_fuel", Items.COAL);
		var menu = helper.openMenu(SmokerMenu::new, new ItemStack(Items.COAL));

		// Operation: transfer the fuel ingredient into a smoker.
		var result = helper.transfer(RecipeTypes.FUELING, recipe, menu, false);

		// Assertions: the smoker fuel slot receives the coal.
		helper.assertSuccessfulTransfer(result, helper::getFurnaceFuelSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getFurnaceResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getFurnaceCraftAreaSlots,
				List.of(
					stackAt(AbstractFurnaceMenu.FUEL_SLOT, Items.COAL)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToBlastFurnaceInput(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: one raw iron stack is available as a blasting ingredient.
		TransferRecipe<?> recipe = basicRecipe("blasting_input", Items.RAW_IRON);
		var menu = helper.openMenu(BlastFurnaceMenu::new, new ItemStack(Items.RAW_IRON));

		// Operation: transfer the blasting ingredient into a blast furnace.
		var result = helper.transfer(RecipeTypes.BLASTING, recipe, menu, false);

		// Assertions: the blast furnace input slot receives the raw iron.
		helper.assertSuccessfulTransfer(result, helper::getFurnaceIngredientSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getFurnaceResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getFurnaceCraftAreaSlots,
				List.of(
					stackAt(AbstractFurnaceMenu.INGREDIENT_SLOT, Items.RAW_IRON)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToBlastFurnaceFuel(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: one coal stack is available as blast furnace fuel.
		TransferRecipe<?> recipe = basicRecipe("blasting_fuel", Items.COAL);
		var menu = helper.openMenu(BlastFurnaceMenu::new, new ItemStack(Items.COAL));

		// Operation: transfer the fuel ingredient into a blast furnace.
		var result = helper.transfer(RecipeTypes.FUELING, recipe, menu, false);

		// Assertions: the blast furnace fuel slot receives the coal.
		helper.assertSuccessfulTransfer(result, helper::getFurnaceFuelSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getFurnaceResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getFurnaceCraftAreaSlots,
				List.of(
					stackAt(AbstractFurnaceMenu.FUEL_SLOT, Items.COAL)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToBrewingStand(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: three bottles and one nether wart are available for brewing.
		TransferRecipe<?> recipe = basicRecipe(
			"brewing",
			Items.GLASS_BOTTLE,
			Items.GLASS_BOTTLE,
			Items.GLASS_BOTTLE,
			Items.NETHER_WART
		);
		var menu = helper.openMenu(
			BrewingStandMenu::new,
			new ItemStack(Items.GLASS_BOTTLE, 3),
			new ItemStack(Items.NETHER_WART)
		);

		// Operation: transfer the brewing recipe into a brewing stand.
		var result = helper.transfer(RecipeTypes.BREWING, recipe, menu, false);

		// Assertions: the bottle slots and ingredient slot receive their recipe items.
		helper.assertSuccessfulTransfer(result, helper::getBrewingRecipeSlots);
		helper.createMenuChecker(result.menu())
			.assertCraftingArea(
				RecipeTransferGameTests::getBrewingSlots,
				List.of(
					stackAt(BREWING_LEFT_BOTTLE_SLOT, Items.GLASS_BOTTLE),
					stackAt(BREWING_CENTER_BOTTLE_SLOT, Items.GLASS_BOTTLE),
					stackAt(BREWING_RIGHT_BOTTLE_SLOT, Items.GLASS_BOTTLE),
					stackAt(BREWING_INGREDIENT_SLOT, Items.NETHER_WART)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToAnvil(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: a sword and an ingot are available as anvil inputs.
		TransferRecipe<?> recipe = basicRecipe("anvil", Items.IRON_SWORD, Items.IRON_INGOT);
		var menu = helper.openMenu(AnvilMenu::new, new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_INGOT));

		// Operation: transfer both inputs into an anvil.
		var result = helper.transfer(RecipeTypes.ANVIL, recipe, menu, false);

		// Assertions: both anvil input slots receive their recipe items.
		helper.assertSuccessfulTransfer(result, helper::getAnvilSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getAnvilResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getAnvilSlots,
				List.of(
					stackAt(AnvilMenu.INPUT_SLOT, Items.IRON_SWORD),
					stackAt(AnvilMenu.ADDITIONAL_SLOT, Items.IRON_INGOT)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersToSmithingTable(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		TransferRecipe<TestRecipe> recipe = basicRecipe("smithing", Items.DIAMOND_SWORD, Items.NETHERITE_INGOT);
		var menu = helper.openMenu(
			SmithingMenu::new,
			new ItemStack(Items.DIAMOND_SWORD),
			new ItemStack(Items.NETHERITE_INGOT)
		);

		var result = helper.transfer(RecipeTypes.SMITHING, recipe, menu, false);

		helper.assertSuccessfulTransfer(result, helper::getSmithingSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getSmithingResultSlots,
				List.of(stackAt(0, Items.NETHERITE_SWORD))
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getSmithingSlots,
				List.of(
					stackAt(0, Items.DIAMOND_SWORD),
					stackAt(1, Items.NETHERITE_INGOT)
				)
			)
			.assertPlayerInventory(List.of())
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersFromSplitStacks(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: two required planks are available as separate inventory stacks.
		TransferRecipe<CraftingRecipe> recipe = craftingRecipe("two_planks", new ItemStack(Items.STICK), grid(
			ingredient(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS)
		));
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.STICK),
			new ItemStack(Items.OAK_PLANKS),
			new ItemStack(Items.OAK_PLANKS)
		);

		// Operation: transfer the two-plank recipe into a crafting table.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: both required crafting grid slots are filled from separate stacks.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.STICK))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersAlternativeIngredient(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the recipe accepts oak or birch, and only birch is available.
		TransferRecipe<CraftingRecipe> recipe = craftingRecipe(
			"alternative_plank",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.items(
					new ItemStack(Items.OAK_PLANKS),
					new ItemStack(Items.BIRCH_PLANKS)
				))
			)
		);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.BIRCH_PLANKS));

		// Operation: transfer the alternative ingredient recipe.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the available birch alternative is accepted.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.BIRCH_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersAlternativeIngredientWithoutStealingRequiredIngredient(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the first slot can use oak or birch, but the second slot must use oak.
		TransferRecipe<CraftingRecipe> recipe = craftingRecipe(
			"constrained_alternative_plank",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.items(
					new ItemStack(Items.OAK_PLANKS),
					new ItemStack(Items.BIRCH_PLANKS)
				)),
				ingredient(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS)
			)
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.OAK_PLANKS),
			new ItemStack(Items.BIRCH_PLANKS)
		);

		// Operation: transfer both ingredients from inventory.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: birch is used for the flexible slot, leaving oak for the required slot.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.BIRCH_PLANKS),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersExactSubtypeIngredient(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: water and healing lingering potions share an item type, but only water matches the recipe.
		ItemStack waterPotion = lingeringPotion(Potions.WATER);
		ItemStack healingPotion = lingeringPotion(Potions.HEALING);
		TransferRecipe<CraftingRecipe> recipe = craftingRecipe(
			"tipped_arrow",
			new ItemStack(Items.TIPPED_ARROW, 8),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, Items.ARROW),
				ingredient(CRAFTING_GRID_TOP_CENTER, Items.ARROW),
				ingredient(CRAFTING_GRID_TOP_RIGHT, Items.ARROW),
				ingredient(CRAFTING_GRID_MIDDLE_LEFT, Items.ARROW),
				ingredient(CRAFTING_GRID_CENTER, TestRecipeSlotView.item(waterPotion)),
				ingredient(CRAFTING_GRID_MIDDLE_RIGHT, Items.ARROW),
				ingredient(CRAFTING_GRID_BOTTOM_LEFT, Items.ARROW),
				ingredient(CRAFTING_GRID_BOTTOM_CENTER, Items.ARROW),
				ingredient(CRAFTING_GRID_BOTTOM_RIGHT, Items.ARROW)
			)
		);
		var menu = helper.openMenu(
			RecipeTransferGameTests::createCraftingMenu,
			new ItemStack(Items.ARROW, 8),
			healingPotion,
			waterPotion
		);

		// Operation: transfer the tipped-arrow recipe with both potion subtypes in inventory.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the water potion moves, while the healing potion remains untouched.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of(stackAt(0, tippedArrow(Potions.WATER, 8)))
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.ARROW),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.ARROW),
					stackAt(CRAFTING_GRID_TOP_RIGHT, Items.ARROW),
					stackAt(CRAFTING_GRID_MIDDLE_LEFT, Items.ARROW),
					stackAt(CRAFTING_GRID_CENTER, waterPotion),
					stackAt(CRAFTING_GRID_MIDDLE_RIGHT, Items.ARROW),
					stackAt(CRAFTING_GRID_BOTTOM_LEFT, Items.ARROW),
					stackAt(CRAFTING_GRID_BOTTOM_CENTER, Items.ARROW),
					stackAt(CRAFTING_GRID_BOTTOM_RIGHT, Items.ARROW)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(1, healingPotion))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void reportsMissingIngredients(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the crafting table recipe needs four planks, but only three are available.
		TransferRecipe<?> recipe = craftingTableRecipe();
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 3));

		// Operation: attempt to transfer the incomplete recipe.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the transfer reports missing ingredients and leaves the grid empty.
		helper.assertFailedTransfer(result, helper::getCraftingGridSlots, RecipeTransferErrorMissingSlots.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 3)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maxTransferMovesMultipleCompleteSets(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: ten planks can satisfy five complete two-plank recipe sets.
		TransferRecipe<TestRecipe> recipe = basicRecipe("max_transfer_sets", Items.OAK_PLANKS, Items.OAK_PLANKS);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 10));

		// Operation: max transfer the two-slot recipe into a crafting table.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, true);

		// Assertions: five complete sets move and the inventory is empty.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 5)),
					stackAt(CRAFTING_GRID_TOP_CENTER, new ItemStack(Items.OAK_PLANKS, 5))
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maxTransferStopsAtLimitingIngredient(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: planks are abundant, but sticks can only satisfy three complete sets.
		TransferRecipe<TestRecipe> recipe = basicRecipe("max_transfer_limited", Items.OAK_PLANKS, Items.STICK);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 10), new ItemStack(Items.STICK, 3));

		// Operation: max transfer should stop when the limiting stick stack runs out.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, true);

		// Assertions: three sets move, with seven planks left and no sticks duplicated.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 3)),
					stackAt(CRAFTING_GRID_TOP_CENTER, new ItemStack(Items.STICK, 3))
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 7)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maxTransferStopsAtNonStackableSlotLimit(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: two swords are available, but the anvil input slot can only hold one.
		TransferRecipe<TestRecipe> recipe = basicRecipe("max_transfer_non_stackable", Items.IRON_SWORD);
		var menu = helper.openMenu(AnvilMenu::new, new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_SWORD));

		// Operation: max transfer into the anvil input.
		var result = helper.transfer(RecipeTypes.ANVIL, recipe, menu, true);

		// Assertions: one sword moves to the anvil and the second remains in inventory.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getAnvilResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getAnvilSlots,
				List.of(
					stackAt(AnvilMenu.INPUT_SLOT, Items.IRON_SWORD)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(1, Items.IRON_SWORD))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maxTransferStopsAtLowStackLimitIngredient(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: ender pearls stack to 16, but planks are available for more recipe sets.
		TransferRecipe<TestRecipe> recipe = basicRecipe(
			"max_transfer_low_stack_limit",
			Items.ENDER_PEARL,
			Items.OAK_PLANKS
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.ENDER_PEARL, 16),
			new ItemStack(Items.ENDER_PEARL, 4),
			new ItemStack(Items.OAK_PLANKS, 20)
		);

		// Operation: max transfer should stop when the pearl target slot reaches its stack limit.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, true);

		// Assertions: sixteen complete sets move, and the extra items are stowed back without duplication.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.ENDER_PEARL, 16)),
					stackAt(CRAFTING_GRID_TOP_CENTER, new ItemStack(Items.OAK_PLANKS, 16))
				)
			)
			.assertPlayerInventory(
				List.of(
					stackAt(0, new ItemStack(Items.ENDER_PEARL, 4)),
					stackAt(1, new ItemStack(Items.OAK_PLANKS, 4))
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maxTransferStopsAtNonStackableIngredientInMultiIngredientRecipe(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: two swords are available, but only one can fit in the recipe target slot.
		TransferRecipe<TestRecipe> recipe = basicRecipe(
			"max_transfer_non_stackable_limited",
			Items.IRON_SWORD,
			Items.OAK_PLANKS
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.IRON_SWORD),
			new ItemStack(Items.IRON_SWORD),
			new ItemStack(Items.OAK_PLANKS, 10)
		);

		// Operation: max transfer should stop at one complete set because the sword slot is full.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, true);

		// Assertions: one set reaches the grid, and the extra sword and planks remain in inventory.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.IRON_SWORD),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of(
					stackAt(0, new ItemStack(Items.OAK_PLANKS, 9)),
					stackAt(1, Items.IRON_SWORD)
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersWithFullInventoryAndFullCraftingGrid(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the inventory is full, but the dirt stacks have room for the nine displaced grid items.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		fillSlots(inventorySlots, new ItemStack(Items.DIRT, 63));
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS, 9));
		fillSlots(getCraftingInputSlots(menu), new ItemStack(Items.DIRT));

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
		// Operation: transfer a 3x3 recipe over a fully occupied grid.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: planks fill the grid and each displaced dirt item is stowed exactly once.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				allSlots(getCraftingInputSlots(menu), new ItemStack(Items.OAK_PLANKS))
			)
			.assertPlayerInventory(
				filledInventory(
					new ItemStack(Items.DIRT, 63),
					List.of(
						stackAt(0, ItemStack.EMPTY),
						stackAt(1, new ItemStack(Items.DIRT, 64)),
						stackAt(2, new ItemStack(Items.DIRT, 64)),
						stackAt(3, new ItemStack(Items.DIRT, 64)),
						stackAt(4, new ItemStack(Items.DIRT, 64)),
						stackAt(5, new ItemStack(Items.DIRT, 64)),
						stackAt(6, new ItemStack(Items.DIRT, 64)),
						stackAt(7, new ItemStack(Items.DIRT, 64)),
						stackAt(8, new ItemStack(Items.DIRT, 64)),
						stackAt(9, new ItemStack(Items.DIRT, 64))
					)
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void reportsInventoryFullWithFullInventoryAndFullCraftingGrid(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: both the inventory and crafting grid are full, leaving no room to clear the grid.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		fillSlots(helper.getStandardInventorySlots(menu), new ItemStack(Items.DIRT, 64));
		helper.getStandardInventorySlots(menu).get(0).set(new ItemStack(Items.OAK_PLANKS));
		fillSlots(getCraftingInputSlots(menu), new ItemStack(Items.DIRT));

		TransferRecipe<TestRecipe> recipe = basicRecipe("full_inventory_full_grid_failure", Items.OAK_PLANKS);
		// Operation: attempt a transfer that would need to displace existing grid items.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the transfer is rejected before any grid or inventory items move.
		helper.assertTransferError(result, RecipeTransferErrorTooltip.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				allSlots(getCraftingInputSlots(menu), new ItemStack(Items.DIRT))
			)
			.assertPlayerInventory(
				filledInventory(
					new ItemStack(Items.DIRT, 64),
					List.of(stackAt(0, Items.OAK_PLANKS))
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersFromOccupiedCraftingGrid(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the only required ingredient is already in the correct crafting slot.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		Slot ingredientSlot = getCraftingInputSlots(menu).get(0);
		ingredientSlot.set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = basicRecipe("already_in_grid", Items.OAK_PLANKS);
		// Operation: transfer from the current menu so grid contents are considered as sources.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the ingredient remains in place and the inventory stays empty.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersFromMovableInventorySlotWhenMatchingSlotIsLocked(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the first matching plank stack cannot be moved, but another matching stack is available.
		ServerPlayer player = helper.getPlayer();
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		Slot lockedSlot = replaceSlot(
			menu,
			inventorySlots.get(0),
			lockedSlot(inventorySlots.get(0))
		);
		lockedSlot.set(new ItemStack(Items.OAK_PLANKS));
		inventorySlots = helper.getStandardInventorySlots(menu);
		Slot sourceSlot = inventorySlots.get(1);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		List<TransferOperation> operations = List.of(
			new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index)
		);

		// Operation: call the server transfer with the movable slot as the source.
		// The normal helper rejects any non-empty locked inventory slot before sending a packet.
		BasicRecipeTransferHandlerServer.setItems(
			player,
			operations,
			craftingSlots,
			inventorySlots,
			false,
			true
		);

		// Assertions: the locked stack remains untouched and no duplicate plank appears in inventory.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void reportsErrorWhenRequiredInventoryItemIsLocked(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the only required ingredient is in an inventory slot that cannot be moved.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		Slot lockedSourceSlot = replaceSlot(
			menu,
			inventorySlots.get(0),
			lockedSlot(inventorySlots.get(0))
		);
		lockedSourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = basicRecipe("locked_required_inventory_item", Items.OAK_PLANKS);
		// Operation: attempt a normal transfer with the required item locked in inventory.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the transfer fails before moving the locked source item.
		helper.assertTransferError(result, RecipeTransferErrorInternal.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void transfersFromWrongCraftingGridSlot(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the required ingredient is in the crafting grid, but not in the recipe's target slot.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = testRecipe(
			"wrong_grid_slot",
			List.of(
				TestRecipeSlotView.empty(),
				TestRecipeSlotView.item(Items.OAK_PLANKS)
			)
		);
		// Operation: transfer from the current menu so the wrong grid slot can be moved.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the old grid slot is empty, the target slot is filled, and nothing is in inventory.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void stowsDisplacedCraftingGridItemsIntoInventoryStacks(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: replacing the grid item should stow dirt into an existing partial dirt stack.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.DIRT));
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS));
		inventorySlots.get(10).set(new ItemStack(Items.DIRT, 63));

		TransferRecipe<TestRecipe> recipe = basicRecipe("stow_displaced_grid_item", Items.OAK_PLANKS);
		// Operation: transfer one plank into the occupied crafting slot.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the plank moves to the grid, and the displaced dirt fills the partial stack.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(10, new ItemStack(Items.DIRT, 64)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void reportsErrorWhenLockedCraftingGridItemBlocksRecipeTransfer(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the recipe needs the top-left slot, but a locked dirt item already occupies it.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot lockedGridSlot = replaceSlot(
			menu,
			craftingSlots.get(CRAFTING_GRID_TOP_LEFT),
			lockedSlot(craftingSlots.get(CRAFTING_GRID_TOP_LEFT))
		);
		lockedGridSlot.set(new ItemStack(Items.DIRT));
		helper.getStandardInventorySlots(menu).get(0).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = basicRecipe("locked_grid_blocks_recipe", Items.OAK_PLANKS);
		// Operation: attempt a normal transfer that would need to clear the locked grid item.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the transfer fails and both the locked grid item and inventory item remain.
		helper.assertTransferError(result, RecipeTransferErrorInternal.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.DIRT)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	@SuppressWarnings("removal")
	public static void doesNotInsertIntoRejectingItemHandlerSlot(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the target is a real item-handler slot configured to reject every ingredient.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		ItemStackHandler rejectingItemHandler = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		replaceSlot(
			menu,
			craftingSlots.get(CRAFTING_GRID_TOP_LEFT),
			itemHandlerSlot(craftingSlots.get(CRAFTING_GRID_TOP_LEFT), rejectingItemHandler)
		);
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = basicRecipe("rejecting_item_handler_slot", Items.OAK_PLANKS);
		// Operation: transfer through the normal helper path so the server receives the generated packet.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the target remains empty and the source item is returned to inventory.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void doesNotInsertIntoCraftingResultSlot(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: a server transfer operation maliciously names the crafting output as its destination.
		ServerPlayer player = helper.getPlayer();
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		Slot sourceSlot = inventorySlots.get(0);
		Slot resultSlot = getCraftingResultSlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		// Operation: execute the invalid transfer through the complete server transfer path.
		BasicRecipeTransferHandlerServer.setItems(
			player,
			List.of(new TransferOperation(sourceSlot.index, resultSlot.index)),
			List.of(resultSlot),
			inventorySlots,
			false,
			true
		);

		// Assertions: the output remains empty and the source ingredient is untouched.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void ignoresMaliciousPacketWithInvalidAllowedSlotId(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		int invalidSlotId = menu.slots.size();
		FriendlyByteBuf packetData = recipeTransferPacketData(
			List.of(new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index)),
			List.of(craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index, invalidSlotId),
			List.of(sourceSlot.index),
			false,
			true
		);
		try {
			helper.sendPacketDataToServer(packetData);
		} finally {
			packetData.release();
		}

		assertSinglePlankUnmoved(helper, menu);
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void ignoresMaliciousPacketWithOversizedSlotList(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		FriendlyByteBuf packetData = new FriendlyByteBuf(Unpooled.buffer());
		packetData.writeByte(PacketIdServer.RECIPE_TRANSFER.ordinal());
		packetData.writeVarInt(1);
		new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index).writePacketData(packetData);
		packetData.writeVarInt(menu.slots.size() + 1);
		try {
			helper.sendPacketDataToServer(packetData);
		} finally {
			packetData.release();
		}

		assertSinglePlankUnmoved(helper, menu);
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void ignoresMaliciousPacketWithInvalidOperationSlotId(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		int invalidSlotId = menu.slots.size();
		FriendlyByteBuf packetData = recipeTransferPacketData(
			List.of(new TransferOperation(invalidSlotId, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index)),
			List.of(craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
			List.of(sourceSlot.index),
			false,
			true
		);
		try {
			helper.sendPacketDataToServer(packetData);
		} finally {
			packetData.release();
		}

		assertSinglePlankUnmoved(helper, menu);
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maliciousPacketCannotDuplicateSingleItemAcrossMultipleTargets(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the packet tries to use the same one-item source stack for two different recipe slots.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index)
			),
			List.of(
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT),
				craftingSlots.get(CRAFTING_GRID_TOP_CENTER)
			),
			List.of(sourceSlot),
			false,
			true
		);

		// Operation: send the forged packet directly to the server packet handler.
		helper.sendPacketToServer(packet);

		// Assertions: the incomplete transfer rolls back instead of duplicating the one source item.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maliciousMaxTransferPacketCannotDuplicateSingleItemAcrossTargets(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: max-transfer is requested with the same one-item source stack repeated for two targets.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index)
			),
			List.of(
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT),
				craftingSlots.get(CRAFTING_GRID_TOP_CENTER)
			),
			List.of(sourceSlot),
			true,
			false
		);

		// Operation: send the forged max-transfer packet directly to the server packet handler.
		helper.sendPacketToServer(packet);

		// Assertions: only the one real item moves, and no second plank is created.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void maliciousMaxTransferPacketUsingTargetAsSourceDoesNotHang(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: the packet points a max-transfer operation from a crafting slot back into itself.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.OAK_PLANKS));

		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(new TransferOperation(
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index,
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index
			)),
			List.of(craftingSlots.get(CRAFTING_GRID_TOP_LEFT)),
			List.of(),
			true,
			false
		);

		// Operation: send the forged packet directly to the server packet handler.
		helper.sendPacketToServer(packet);

		// Assertions: the test completes and the one crafting-grid item is still present exactly once.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void rejectsTooLargePlayerCraftingRecipe(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: a recipe uses a 3x3-only slot, but the target menu is the player's 2x2 grid.
		TransferRecipe<CraftingRecipe> recipe = craftingRecipe(
			"too_large_player_inventory",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_BOTTOM_RIGHT, Items.OAK_PLANKS)
			)
		);
		var menu = helper.openMenu(
			(containerId, inventory) -> inventory.player.inventoryMenu,
			new ItemStack(Items.OAK_PLANKS)
		);

		// Operation: attempt to transfer the oversized recipe into the player inventory menu.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the helper detects the outside-grid ingredient and reports a transfer error.
		helper.assertRecipeHasIngredientsOutsidePlayerGrid(result);
		helper.assertFailedTransfer(result, helper::getCraftingGridSlots, RecipeTransferErrorTooltip.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getPlayerCraftingInputSlots,
				List.of()
			)
			.assertPlayerInventory(
				RecipeTransferGameTests::getPlayerInventorySlots,
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void rollsBackIncompleteCompleteSet(GameTestHelper gameTestHelper) {
		RecipeTransferTestHelper helper = new RecipeTransferTestHelper(gameTestHelper);
		// Setup: max transfer can start a second set with planks, but there is no second stick.
		ServerPlayer player = helper.getPlayer();
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);

		Slot planksSlot = helper.getStandardInventorySlots(menu).get(0);
		Slot sticksSlot = helper.getStandardInventorySlots(menu).get(1);
		planksSlot.set(new ItemStack(Items.OAK_PLANKS, 2));
		sticksSlot.set(new ItemStack(Items.STICK, 1));

		List<Slot> craftingSlots = getCraftingInputSlots(menu);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		List<TransferOperation> operations = List.of(
			new TransferOperation(planksSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
			new TransferOperation(sticksSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index)
		);

		// Operation: call the server transfer directly with complete-set rollback enabled.
		BasicRecipeTransferHandlerServer.setItems(
			player,
			operations,
			craftingSlots,
			inventorySlots,
			true,
			true
		);

		// Assertions: only one complete set is transferred, and the attempted extra plank is restored.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCraftingInputSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.STICK)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	private static FriendlyByteBuf recipeTransferPacketData(
		List<TransferOperation> transferOperations,
		List<Integer> craftingSlotIds,
		List<Integer> inventorySlotIds,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		FriendlyByteBuf packetData = new FriendlyByteBuf(Unpooled.buffer());
		packetData.writeByte(PacketIdServer.RECIPE_TRANSFER.ordinal());
		packetData.writeVarInt(transferOperations.size());
		for (TransferOperation operation : transferOperations) {
			operation.writePacketData(packetData);
		}
		packetData.writeVarInt(craftingSlotIds.size());
		for (int slotId : craftingSlotIds) {
			packetData.writeVarInt(slotId);
		}
		packetData.writeVarInt(inventorySlotIds.size());
		for (int slotId : inventorySlotIds) {
			packetData.writeVarInt(slotId);
		}
		packetData.writeBoolean(maxTransfer);
		packetData.writeBoolean(requireCompleteSets);
		return packetData;
	}

	private static void assertSinglePlankUnmoved(RecipeTransferTestHelper helper, CraftingMenu menu) {
		helper.createMenuChecker(menu)
			.assertResults(RecipeTransferGameTests::getCraftingResultSlots, List.of())
			.assertCraftingArea(RecipeTransferGameTests::getCraftingInputSlots, List.of())
			.assertPlayerInventory(List.of(stackAt(0, Items.OAK_PLANKS)))
			.assertAllSlotsChecked();
	}

	private static TransferRecipe<CraftingRecipe> craftingTableRecipe() {
		return craftingRecipe("crafting_table", new ItemStack(Items.CRAFTING_TABLE), grid(
			ingredient(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_MIDDLE_LEFT, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_CENTER, Items.OAK_PLANKS)
		));
	}

	private static void fillSlots(List<Slot> slots, ItemStack stack) {
		for (Slot slot : slots) {
			slot.set(stack.copy());
		}
	}

	private static Slot replaceSlot(AbstractContainerMenu menu, Slot originalSlot, Slot replacementSlot) {
		replacementSlot.index = originalSlot.index;
		menu.slots.set(originalSlot.index, replacementSlot);
		return replacementSlot;
	}

	private static CraftingMenu createCraftingMenu(int containerId, Inventory inventory) {
		ContainerLevelAccess access = ContainerLevelAccess.create(inventory.player.level, inventory.player.blockPosition());
		return new CraftingMenu(containerId, inventory, access);
	}

	private static Slot lockedSlot(Slot slot) {
		return new Slot(slot.container, slot.getContainerSlot(), slot.x, slot.y) {
			@Override
			public boolean mayPickup(Player player) {
				return false;
			}
		};
	}

	@SuppressWarnings("removal")
	private static Slot itemHandlerSlot(Slot slot, ItemStackHandler itemHandler) {
		return new SlotItemHandler(itemHandler, 0, slot.x, slot.y);
	}

	private static List<Slot> getCraftingInputSlots(RecipeBookMenu<?> menu) {
		return menu.slots.subList(menu.getResultSlotIndex() + 1, menu.getSize());
	}

	private static List<Slot> getPlayerCraftingInputSlots(InventoryMenu menu) {
		return getCraftingInputSlots(menu);
	}

	private static List<Slot> getCraftingResultSlots(RecipeBookMenu<?> menu) {
		return List.of(menu.getSlot(menu.getResultSlotIndex()));
	}

	private static List<Slot> getPlayerInventorySlots(InventoryMenu menu) {
		List<Slot> inventorySlots = new ArrayList<>();
		inventorySlots.addAll(menu.slots.subList(InventoryMenu.ARMOR_SLOT_END, InventoryMenu.SHIELD_SLOT));
		inventorySlots.addAll(menu.slots.subList(InventoryMenu.ARMOR_SLOT_START, InventoryMenu.ARMOR_SLOT_END));
		inventorySlots.add(menu.getSlot(InventoryMenu.SHIELD_SLOT));
		return inventorySlots;
	}

	private static List<Slot> getFurnaceCraftAreaSlots(AbstractFurnaceMenu menu) {
		return List.of(
			menu.getSlot(AbstractFurnaceMenu.INGREDIENT_SLOT),
			menu.getSlot(AbstractFurnaceMenu.FUEL_SLOT)
		);
	}

	private static List<Slot> getFurnaceResultSlots(AbstractFurnaceMenu menu) {
		return List.of(menu.getSlot(AbstractFurnaceMenu.RESULT_SLOT));
	}

	private static List<Slot> getBrewingSlots(BrewingStandMenu menu) {
		return List.of(
			menu.getSlot(BREWING_LEFT_BOTTLE_SLOT),
			menu.getSlot(BREWING_CENTER_BOTTLE_SLOT),
			menu.getSlot(BREWING_RIGHT_BOTTLE_SLOT),
			menu.getSlot(BREWING_INGREDIENT_SLOT),
			menu.getSlot(BREWING_FUEL_SLOT)
		);
	}

	private static List<Slot> getAnvilSlots(AnvilMenu menu) {
		return List.of(
			menu.getSlot(AnvilMenu.INPUT_SLOT),
			menu.getSlot(AnvilMenu.ADDITIONAL_SLOT)
		);
	}

	private static List<Slot> getAnvilResultSlots(AnvilMenu menu) {
		return List.of(menu.getSlot(AnvilMenu.RESULT_SLOT));
	}

	private static List<Slot> getSmithingSlots(SmithingMenu menu) {
		return List.of(menu.getSlot(0), menu.getSlot(1));
	}

	private static List<Slot> getSmithingResultSlots(SmithingMenu menu) {
		return List.of(menu.getSlot(2));
	}

	private static List<StackPlacement> filledInventory(ItemStack stack, List<StackPlacement> placements) {
		return filledSlots(Inventory.INVENTORY_SIZE, stack, placements);
	}

	private static List<StackPlacement> allSlots(List<Slot> slots, ItemStack stack) {
		return filledSlots(slots.size(), stack, List.of());
	}

	private static List<StackPlacement> filledSlots(int size, ItemStack stack, List<StackPlacement> placements) {
		List<ItemStack> stacks = JeiGameTestHelper.getFilledStacks(size, stack, placements);
		List<StackPlacement> stackPlacements = new ArrayList<>();
		for (int i = 0; i < stacks.size(); i++) {
			stackPlacements.add(stackAt(i, stacks.get(i)));
		}
		return stackPlacements;
	}

	private static ItemStack lingeringPotion(Potion potion) {
		ItemStack stack = new ItemStack(Items.LINGERING_POTION);
		PotionUtils.setPotion(stack, potion);
		return stack;
	}

	private static ItemStack tippedArrow(Potion potion, int count) {
		ItemStack stack = new ItemStack(Items.TIPPED_ARROW, count);
		PotionUtils.setPotion(stack, potion);
		return stack;
	}


}
