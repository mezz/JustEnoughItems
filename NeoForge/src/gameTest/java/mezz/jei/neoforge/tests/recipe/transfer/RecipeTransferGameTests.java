package mezz.jei.neoforge.tests.recipe.transfer;

import static mezz.jei.neoforge.tests.lib.TestRecipes.basicRecipe;
import static mezz.jei.neoforge.tests.lib.TestRecipes.craftingRecipe;
import static mezz.jei.neoforge.tests.lib.TestRecipes.grid;
import static mezz.jei.neoforge.tests.lib.TestRecipes.ingredient;
import static mezz.jei.neoforge.tests.lib.TestRecipes.testRecipe;
import static mezz.jei.neoforge.tests.lib.StackPlacement.stackAt;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.TransferOperation;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import mezz.jei.neoforge.tests.lib.JeiGameTestHelper;
import mezz.jei.neoforge.tests.lib.StackPlacement;
import mezz.jei.neoforge.tests.lib.TestRecipes.TestRecipe;
import mezz.jei.neoforge.tests.lib.TestRecipeSlotView;
import mezz.jei.neoforge.tests.lib.TransferRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

import java.util.ArrayList;
import java.util.List;

@ForEachTest(groups = "recipe_transfer")
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
	private static final int CRAFTER_RESULT_SLOT = 45;

	private RecipeTransferGameTests() {
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers recipe ingredients into a crafting table.")
	public static void transfersToCraftingTable(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers recipe ingredients into the player's 2x2 crafting grid.")
	public static void transfersToPlayerCraftingGrid(RecipeTransferTestHelper helper) {
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
				InventoryMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers recipe ingredients into a crafter.")
	public static void transfersToCrafter(RecipeTransferTestHelper helper) {
		// Setup: a crafting recipe has all four planks available in inventory.
		TransferRecipe<?> recipe = craftingTableRecipe();
		var menu = helper.openMenu(CrafterMenu::new, new ItemStack(Items.OAK_PLANKS, 4));

		// Operation: transfer the recipe into a crafter menu.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the crafter receives the recipe ingredients.
		helper.assertSuccessfulTransfer(result, helper::getCrafterSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCrafterResultSlots,
				List.of()
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getCrafterSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a smelting input into a furnace.")
	public static void transfersToFurnaceInput(RecipeTransferTestHelper helper) {
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers smelting fuel into a furnace.")
	public static void transfersToFurnaceFuel(RecipeTransferTestHelper helper) {
		// Setup: one coal stack is available as furnace fuel.
		TransferRecipe<?> recipe = basicRecipe("smelting_fuel", Items.COAL);
		var menu = helper.openMenu(FurnaceMenu::new, new ItemStack(Items.COAL));

		// Operation: transfer the fuel ingredient into a furnace.
		var result = helper.transfer(RecipeTypes.SMELTING_FUEL, recipe, menu, false);

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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a smoking input into a smoker.")
	public static void transfersToSmokerInput(RecipeTransferTestHelper helper) {
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers smoking fuel into a smoker.")
	public static void transfersToSmokerFuel(RecipeTransferTestHelper helper) {
		// Setup: one coal stack is available as smoker fuel.
		TransferRecipe<?> recipe = basicRecipe("smoking_fuel", Items.COAL);
		var menu = helper.openMenu(SmokerMenu::new, new ItemStack(Items.COAL));

		// Operation: transfer the fuel ingredient into a smoker.
		var result = helper.transfer(RecipeTypes.SMOKING_FUEL, recipe, menu, false);

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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a blasting input into a blast furnace.")
	public static void transfersToBlastFurnaceInput(RecipeTransferTestHelper helper) {
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers blasting fuel into a blast furnace.")
	public static void transfersToBlastFurnaceFuel(RecipeTransferTestHelper helper) {
		// Setup: one coal stack is available as blast furnace fuel.
		TransferRecipe<?> recipe = basicRecipe("blasting_fuel", Items.COAL);
		var menu = helper.openMenu(BlastFurnaceMenu::new, new ItemStack(Items.COAL));

		// Operation: transfer the fuel ingredient into a blast furnace.
		var result = helper.transfer(RecipeTypes.BLASTING_FUEL, recipe, menu, false);

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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers bottles and an ingredient into a brewing stand.")
	public static void transfersToBrewingStand(RecipeTransferTestHelper helper) {
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers inputs into an anvil.")
	public static void transfersToAnvil(RecipeTransferTestHelper helper) {
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers inputs into a smithing table.")
	public static void transfersToSmithingTable(RecipeTransferTestHelper helper) {
		// Setup: the template, base, and addition stacks are available as smithing inputs.
		TransferRecipe<?> recipe = basicRecipe(
			"smithing",
			Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
			Items.DIAMOND_SWORD,
			Items.NETHERITE_INGOT
		);
		var menu = helper.openMenu(
			SmithingMenu::new,
			new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
			new ItemStack(Items.DIAMOND_SWORD),
			new ItemStack(Items.NETHERITE_INGOT)
		);

		// Operation: transfer all smithing inputs into a smithing table.
		var result = helper.transfer(RecipeTypes.SMITHING, recipe, menu, false);

		// Assertions: each smithing input slot receives its recipe item.
		helper.assertSuccessfulTransfer(result, helper::getSmithingSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getSmithingResultSlots,
				List.of(stackAt(0, Items.NETHERITE_SWORD))
			)
			.assertCraftingArea(
				RecipeTransferGameTests::getSmithingSlots,
				List.of(
					stackAt(SmithingMenu.TEMPLATE_SLOT, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
					stackAt(SmithingMenu.BASE_SLOT, Items.DIAMOND_SWORD),
					stackAt(SmithingMenu.ADDITIONAL_SLOT, Items.NETHERITE_INGOT)
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a recipe from split inventory stacks.")
	public static void transfersFromSplitStacks(RecipeTransferTestHelper helper) {
		// Setup: two required planks are available as separate inventory stacks.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe("two_planks", new ItemStack(Items.STICK), grid(
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers multiple items into one recipe slot from one inventory stack.")
	public static void transfersStackedIngredientFromSingleStack(RecipeTransferTestHelper helper) {
		// Setup: one recipe slot requires four planks, and one inventory stack has more than enough.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("stacked_planks_single_stack", 4);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 6));

		// Operation: transfer one recipe set into an empty crafting table.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: four planks move into the grid and the original stack keeps the remainder.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 4))
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 2)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers ordinary recipes to servers that do not support counted recipe transfer.")
	public static void transfersUncountedRecipeWithoutCountedTransferPacket(RecipeTransferTestHelper helper) {
		// Setup: a normal one-item-per-slot recipe is available, and counted transfer packets are unsupported.
		TransferRecipe<?> recipe = craftingTableRecipe();
		var serverConnection = helper.createConnectionWithoutCountedTransferPacket();
		var menu = helper.openMenu(RecipeTransferGameTests::createCraftingMenu, new ItemStack(Items.OAK_PLANKS, 4));

		// Operation: transfer through the uncounted packet path.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false, serverConnection);

		// Assertions: ordinary recipe transfer still succeeds without counted packet support.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of(stackAt(0, Items.CRAFTING_TABLE))
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(
		description = "Uses the uncounted recipe transfer packet when counted transfer is not supported by the server."
	)
	public static void usesUncountedPacketWhenCountedRecipeTransferUnsupported(RecipeTransferTestHelper helper) {
		// Setup: counted transfer is unavailable, so the count is not preserved by the packet.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("uncounted_packet_counted_transfer", 3);
		var serverConnection = helper.createConnectionWithoutCountedTransferPacket();
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 3));

		// Operation: transfer through the uncounted packet path.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false, serverConnection);

		// Assertions: only one item moves and the remaining two stay in inventory.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 2)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers multiple items into one recipe slot from split inventory stacks.")
	public static void transfersStackedIngredientFromSplitStacks(RecipeTransferTestHelper helper) {
		// Setup: the three required planks are split across two inventory stacks.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("stacked_planks_split_stacks", 3);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 2), new ItemStack(Items.OAK_PLANKS));

		// Operation: transfer one recipe set into an empty crafting table.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: both source stacks are consumed and the target slot receives the combined count.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 3))
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers a counted ingredient from sparse inventory slots.")
	public static void transfersStackedIngredientFromSparseInventorySlots(RecipeTransferTestHelper helper) {
		// Setup: source planks are separated by unrelated stacks, including a source in the last hotbar slot.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		inventorySlots.get(4).set(new ItemStack(Items.DIRT, 11));
		inventorySlots.get(14).set(new ItemStack(Items.OAK_PLANKS, 2));
		inventorySlots.get(28).set(new ItemStack(Items.COBBLESTONE, 5));
		inventorySlots.get(34).set(new ItemStack(Items.TORCH, 8));
		inventorySlots.get(Inventory.INVENTORY_SIZE - 1).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("stacked_planks_sparse_inventory", 3);
		// Operation: transfer one counted recipe slot from the open menu.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: both plank stacks are consumed, sparse unrelated stacks stay put, and no new stacks appear.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 3))
				)
			)
			.assertPlayerInventory(
				List.of(
					stackAt(4, new ItemStack(Items.DIRT, 11)),
					stackAt(28, new ItemStack(Items.COBBLESTONE, 5)),
					stackAt(34, new ItemStack(Items.TORCH, 8))
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Completes a stacked recipe-slot ingredient that is already partly in the target slot.")
	public static void fillsStackedIngredientAlreadyInTargetSlot(RecipeTransferTestHelper helper) {
		// Setup: the correct target slot already contains part of the required counted stack.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.OAK_PLANKS, 2));
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("stacked_planks_partly_in_target", 3);
		// Operation: transfer from the open menu so the existing grid contents are considered.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the existing two planks are reused, one more plank is added, and inventory is empty.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 3))
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers counted ingredients into multiple recipe slots from shared inventory stacks.")
	public static void transfersStackedIngredientsToMultipleSlotsFromSharedStacks(RecipeTransferTestHelper helper) {
		// Setup: two recipe slots each need two planks, but the inventory is split as 3 + 1.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
			"stacked_planks_multiple_slots",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.item(new ItemStack(Items.OAK_PLANKS, 2))),
				ingredient(CRAFTING_GRID_TOP_CENTER, TestRecipeSlotView.item(new ItemStack(Items.OAK_PLANKS, 2)))
			)
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.OAK_PLANKS, 3),
			new ItemStack(Items.OAK_PLANKS)
		);

		// Operation: transfer one recipe set that must split the first stack across recipe slots.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: both recipe slots receive two planks and no planks remain in inventory.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 2)),
					stackAt(CRAFTING_GRID_TOP_CENTER, new ItemStack(Items.OAK_PLANKS, 2))
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(
		description = "Transfers a counted ingredient with matching components without consuming another subtype."
	)
	public static void transfersCountedSubtypeIngredient(RecipeTransferTestHelper helper) {
		// Setup: both stacks are tipped arrows, but only the water-arrow stack matches the recipe.
		ItemStack waterArrows = tippedArrow(Potions.WATER, 3);
		ItemStack healingArrows = tippedArrow(Potions.HEALING, 3);
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
			"counted_tipped_arrows",
			new ItemStack(Items.ARROW),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.item(waterArrows))
			)
		);
		var menu = helper.openMenu(CraftingMenu::new, healingArrows, waterArrows);

		// Operation: transfer the counted subtype ingredient.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the matching water arrows move, and the healing arrows are untouched.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, waterArrows)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, healingArrows))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Moves multiple recipe-slot items from the wrong crafting grid slot.")
	public static void movesStackedIngredientFromWrongCraftingGridSlot(RecipeTransferTestHelper helper) {
		// Setup: two matching planks are in the grid but in the wrong slot; one more is in inventory.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.OAK_PLANKS, 2));
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
			"stacked_planks_wrong_grid_slot",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_CENTER, TestRecipeSlotView.item(new ItemStack(Items.OAK_PLANKS, 3)))
			)
		);
		// Operation: transfer from the open menu so the wrong grid slot can be used as a source.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: the wrong slot is cleared, the target slot has all three planks, and inventory is empty.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_CENTER, new ItemStack(Items.OAK_PLANKS, 3))
				)
			)
			.assertPlayerInventory(
				List.of()
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(
		description = "Reports missing ingredients when split stacks do not have enough items for one recipe slot."
	)
	public static void reportsMissingStackedIngredientCount(RecipeTransferTestHelper helper) {
		// Setup: one recipe slot needs three planks, but inventory only has two split planks.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("missing_stacked_planks", 3);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS));

		// Operation: attempt to transfer the counted recipe into a crafting table.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the transfer fails with the missing-slots error and does not move items.
		helper.assertFailedTransfer(result, helper::getCraftingGridSlots, RecipeTransferErrorMissingSlots.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(
					stackAt(0, Items.OAK_PLANKS),
					stackAt(1, Items.OAK_PLANKS)
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers an alternative stacked ingredient with its own required count.")
	public static void transfersAlternativeStackedIngredientWithDifferentCounts(RecipeTransferTestHelper helper) {
		// Setup: oak would need three items, but only the birch alternative has its required count.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
			"alternative_stacked_plank_counts",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.items(
					new ItemStack(Items.OAK_PLANKS, 3),
					new ItemStack(Items.BIRCH_PLANKS, 2)
				))
			)
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.OAK_PLANKS, 2),
			new ItemStack(Items.BIRCH_PLANKS, 2)
		);

		// Operation: transfer the recipe with both alternatives present.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: birch is chosen, and the insufficient oak stack is not consumed.
		helper.assertSuccessfulTransfer(result, helper::getCraftingGridSlots);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.BIRCH_PLANKS, 2))
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 2)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports missing ingredients when no stacked alternative has enough items.")
	public static void reportsMissingAlternativeStackedIngredientCount(RecipeTransferTestHelper helper) {
		// Setup: neither the oak nor birch alternative has enough items for the counted ingredient.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
			"missing_alternative_stacked_plank_counts",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.items(
					new ItemStack(Items.OAK_PLANKS, 3),
					new ItemStack(Items.BIRCH_PLANKS, 2)
				))
			)
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.OAK_PLANKS, 2),
			new ItemStack(Items.BIRCH_PLANKS)
		);

		// Operation: attempt to transfer the alternative counted recipe.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, false);

		// Assertions: the transfer fails because every alternative is short.
		helper.assertFailedTransfer(result, helper::getCraftingGridSlots, RecipeTransferErrorMissingSlots.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(
					stackAt(0, new ItemStack(Items.OAK_PLANKS, 2)),
					stackAt(1, Items.BIRCH_PLANKS)
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers an available alternative ingredient.")
	public static void transfersAlternativeIngredient(RecipeTransferTestHelper helper) {
		// Setup: the recipe accepts oak or birch, and only birch is available.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Keeps a scarce ingredient available for recipe slots that require it.")
	public static void transfersAlternativeIngredientWithoutStealingRequiredIngredient(RecipeTransferTestHelper helper) {
		// Setup: the first slot can use oak or birch, but the second slot must use oak.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers the matching item subtype instead of the same item with different components.")
	public static void transfersExactSubtypeIngredient(RecipeTransferTestHelper helper) {
		// Setup: water and healing lingering potions share an item type, but only water matches the recipe.
		ItemStack waterPotion = lingeringPotion(Potions.WATER);
		ItemStack healingPotion = lingeringPotion(Potions.HEALING);
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports a user-facing error when recipe ingredients are missing.")
	public static void reportsMissingIngredients(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 3)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer moves as many complete recipe sets as possible.")
	public static void maxTransferMovesMultipleCompleteSets(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer moves complete sets for stacked recipe-slot ingredients.")
	public static void maxTransferMovesStackedIngredientCompleteSets(RecipeTransferTestHelper helper) {
		// Setup: each complete recipe set needs two planks and one stick.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
			"max_transfer_stacked_planks",
			new ItemStack(Items.STICK),
			grid(
				ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.item(new ItemStack(Items.OAK_PLANKS, 2))),
				ingredient(CRAFTING_GRID_TOP_CENTER, Items.STICK)
			)
		);
		var menu = helper.openMenu(
			CraftingMenu::new,
			new ItemStack(Items.OAK_PLANKS, 5),
			new ItemStack(Items.STICK, 3)
		);

		// Operation: max transfer should move only complete sets, limited here by the planks.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, true);

		// Assertions: two complete sets move, with one plank and one stick left in inventory.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 4)),
					stackAt(CRAFTING_GRID_TOP_CENTER, new ItemStack(Items.STICK, 2))
				)
			)
			.assertPlayerInventory(
				List.of(
					stackAt(0, Items.OAK_PLANKS),
					stackAt(1, Items.STICK)
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer stops before overfilling a stacked recipe-slot ingredient.")
	public static void maxTransferStackedIngredientStopsAtSlotLimit(RecipeTransferTestHelper helper) {
		// Setup: one recipe slot needs three planks per set and one inventory stack is full.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe(
			"max_transfer_stacked_planks_slot_limit",
			3
		);
		var menu = helper.openMenu(CraftingMenu::new, new ItemStack(Items.OAK_PLANKS, 64));

		// Operation: max transfer should stop before the target slot would exceed 64.
		var result = helper.transfer(RecipeTypes.CRAFTING, recipe, menu, true);

		// Assertions: 21 complete sets move to the grid and one plank remains.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 63))
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer stops at the limiting ingredient count.")
	public static void maxTransferStopsAtLimitingIngredient(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer respects non-stackable recipe slots.")
	public static void maxTransferStopsAtNonStackableSlotLimit(RecipeTransferTestHelper helper) {
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer stops at a low stack limit ingredient.")
	public static void maxTransferStopsAtLowStackLimitIngredient(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Max transfer stops when one ingredient is non-stackable.")
	public static void maxTransferStopsAtNonStackableIngredientInMultiIngredientRecipe(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers into a full crafting grid when a full inventory can stow the displaced items.")
	public static void transfersWithFullInventoryAndFullCraftingGrid(RecipeTransferTestHelper helper) {
		// Setup: the inventory is full, but the dirt stacks have room for the nine displaced grid items.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		fillSlots(inventorySlots, new ItemStack(Items.DIRT, 63));
		inventorySlots.getFirst().set(new ItemStack(Items.OAK_PLANKS, 9));
		fillSlots(menu.getInputGridSlots(), new ItemStack(Items.DIRT));

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
				CraftingMenu::getInputGridSlots,
				allSlots(menu.getInputGridSlots(), new ItemStack(Items.OAK_PLANKS))
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports inventory full when a full crafting grid cannot be cleared into a full inventory.")
	public static void reportsInventoryFullWithFullInventoryAndFullCraftingGrid(RecipeTransferTestHelper helper) {
		// Setup: both the inventory and crafting grid are full, leaving no room to clear the grid.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		fillSlots(helper.getStandardInventorySlots(menu), new ItemStack(Items.DIRT, 64));
		helper.getStandardInventorySlots(menu).getFirst().set(new ItemStack(Items.OAK_PLANKS));
		fillSlots(menu.getInputGridSlots(), new ItemStack(Items.DIRT));

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
				CraftingMenu::getInputGridSlots,
				allSlots(menu.getInputGridSlots(), new ItemStack(Items.DIRT))
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports inventory full before a sparse crafting recipe can displace a full crafting grid.")
	public static void reportsInventoryFullForSparseCraftingRecipeWithFullGrid(RecipeTransferTestHelper helper) {
		// Setup: the recipe only uses one grid slot, but all nine grid slots would need clearing.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		fillSlots(inventorySlots, new ItemStack(Items.DIRT, 64));
		inventorySlots.getFirst().set(new ItemStack(Items.OAK_PLANKS, 3));
		fillSlots(menu.getInputGridSlots(), new ItemStack(Items.DIRT));

		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe(
			"full_inventory_sparse_counted_crafting_failure",
			3
		);
		// Operation: attempt a sparse counted transfer with no space for displaced items.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: this must fail before clearing the grid, otherwise dirt could be dropped or deleted.
		helper.assertTransferError(result, RecipeTransferErrorTooltip.class);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				allSlots(menu.getInputGridSlots(), new ItemStack(Items.DIRT))
			)
			.assertPlayerInventory(
				filledInventory(
					new ItemStack(Items.DIRT, 64),
					List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 3)))
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Stows a displaced counted crafting-grid stack into an existing inventory stack.")
	public static void stowsDisplacedCountedCraftingStackIntoInventory(RecipeTransferTestHelper helper) {
		// Setup: the target slot contains 24 dirt, and only one inventory dirt stack has room for it.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		fillSlots(inventorySlots, new ItemStack(Items.COBBLESTONE, 64));
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS, 3));
		inventorySlots.get(1).set(new ItemStack(Items.DIRT, 40));
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.DIRT, 24));

		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = stackedPlanksRecipe("stow_displaced_counted_grid_stack", 3);
		// Operation: transfer the counted plank recipe into the occupied target slot.
		var result = helper.transfer(
			RecipeTypes.CRAFTING,
			recipe,
			menu
		);

		// Assertions: planks replace the grid stack, dirt is stowed into the partial stack, and no cobble moves.
		helper.assertTransferSucceeded(result);
		helper.createMenuChecker(result.menu())
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 3))
				)
			)
			.assertPlayerInventory(
				filledInventory(
					new ItemStack(Items.COBBLESTONE, 64),
					List.of(
						stackAt(0, ItemStack.EMPTY),
						stackAt(1, new ItemStack(Items.DIRT, 64))
					)
				)
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers using an ingredient that is already in the crafting grid.")
	public static void transfersFromOccupiedCraftingGrid(RecipeTransferTestHelper helper) {
		// Setup: the only required ingredient is already in the correct crafting slot.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		Slot ingredientSlot = menu.getInputGridSlots().getFirst();
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Server transfers from a movable inventory slot when another matching slot is locked.")
	public static void transfersFromMovableInventorySlotWhenMatchingSlotIsLocked(RecipeTransferTestHelper helper) {
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

		List<Slot> craftingSlots = menu.getInputGridSlots();
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports an error when the required inventory item cannot be moved.")
	public static void reportsErrorWhenRequiredInventoryItemIsLocked(RecipeTransferTestHelper helper) {
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
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Transfers an ingredient from the wrong crafting grid slot.")
	public static void transfersFromWrongCraftingGridSlot(RecipeTransferTestHelper helper) {
		// Setup: the required ingredient is in the crafting grid, but not in the recipe's target slot.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Stows displaced crafting grid items into matching inventory stacks.")
	public static void stowsDisplacedCraftingGridItemsIntoInventoryStacks(RecipeTransferTestHelper helper) {
		// Setup: replacing the grid item should stow dirt into an existing partial dirt stack.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Reports an error when a locked crafting-grid item blocks the recipe.")
	public static void reportsErrorWhenLockedCraftingGridItemBlocksRecipeTransfer(RecipeTransferTestHelper helper) {
		// Setup: the recipe needs the top-left slot, but a locked dirt item already occupies it.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Does not delete items when a crafting slot refuses the ingredient.")
	public static void doesNotDeleteItemWhenCraftingSlotRejectsIngredient(RecipeTransferTestHelper helper) {
		// Setup: the target crafting slot refuses items, but it is still registered as a recipe slot.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		replaceSlot(
			menu,
			craftingSlots.get(CRAFTING_GRID_TOP_LEFT),
			rejectingSlot(craftingSlots.get(CRAFTING_GRID_TOP_LEFT))
		);
		inventorySlots.get(0).set(new ItemStack(Items.OAK_PLANKS));

		TransferRecipe<TestRecipe> recipe = basicRecipe("rejecting_crafting_slot", Items.OAK_PLANKS);
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
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Ignores malicious recipe transfer packets with invalid allowed slot ids.")
	public static void ignoresMaliciousPacketWithInvalidAllowedSlotId(RecipeTransferTestHelper helper) {
		// Setup: the packet names a valid source and target, but also includes an invalid crafting slot id.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		int invalidSlotId = menu.slots.size();
		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index)),
			List.of(craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index, invalidSlotId),
			List.of(sourceSlot.index),
			false,
			true
		);

		// Operation: send the forged packet directly to the server packet handler.
		helper.sendPacketToServer(packet);

		// Assertions: the invalid packet is ignored before moving any items.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Ignores malicious recipe transfer operations with invalid slot ids.")
	public static void ignoresMaliciousPacketWithInvalidOperationSlotId(RecipeTransferTestHelper helper) {
		// Setup: the allowed slot lists are valid, but the transfer operation references an invalid source.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		int invalidSlotId = menu.slots.size();
		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(new TransferOperation(invalidSlotId, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index)),
			List.of(craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
			List.of(sourceSlot.index),
			false,
			true
		);

		// Operation: send the forged packet directly to the server packet handler.
		helper.sendPacketToServer(packet);

		// Assertions: the invalid operation is rejected without deleting or moving the source item.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Does not duplicate one item across multiple targets from a malicious packet.")
	public static void maliciousPacketCannotDuplicateSingleItemAcrossMultipleTargets(RecipeTransferTestHelper helper) {
		// Setup: the packet tries to use the same one-item source stack for two different recipe slots.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index)
			),
			List.of(
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index,
				craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index
			),
			List.of(sourceSlot.index),
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
				CraftingMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Does not duplicate one item across max-transfer targets from a malicious packet.")
	public static void maliciousMaxTransferPacketCannotDuplicateSingleItemAcrossTargets(RecipeTransferTestHelper helper) {
		// Setup: max-transfer is requested with the same one-item source stack repeated for two targets.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		Slot sourceSlot = helper.getStandardInventorySlots(menu).get(0);
		sourceSlot.set(new ItemStack(Items.OAK_PLANKS));

		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
				new TransferOperation(sourceSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index)
			),
			List.of(
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index,
				craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index
			),
			List.of(sourceSlot.index),
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Does not hang on a max-transfer packet that uses a target slot as its own source.")
	public static void maliciousMaxTransferPacketUsingTargetAsSourceDoesNotHang(RecipeTransferTestHelper helper) {
		// Setup: the packet points a max-transfer operation from a crafting slot back into itself.
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);
		List<Slot> craftingSlots = menu.getInputGridSlots();
		craftingSlots.get(CRAFTING_GRID_TOP_LEFT).set(new ItemStack(Items.OAK_PLANKS));

		PacketRecipeTransfer packet = new PacketRecipeTransfer(
			List.of(new TransferOperation(
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index,
				craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index
			)),
			List.of(craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index),
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Rejects crafting recipes that do not fit in the player's 2x2 grid.")
	public static void rejectsTooLargePlayerCraftingRecipe(RecipeTransferTestHelper helper) {
		// Setup: a recipe uses a 3x3-only slot, but the target menu is the player's 2x2 grid.
		TransferRecipe<RecipeHolder<CraftingRecipe>> recipe = craftingRecipe(
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
				InventoryMenu::getInputGridSlots,
				List.of()
			)
			.assertPlayerInventory(
				RecipeTransferGameTests::getPlayerInventorySlots,
				List.of(stackAt(0, Items.OAK_PLANKS))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	@GameTest
	@EmptyTemplate
	@TestHolder(description = "Rolls back partial extra transfers when complete sets are required.")
	public static void rollsBackIncompleteCompleteSet(RecipeTransferTestHelper helper) {
		// Setup: max transfer can start a second set with planks, but there is no second stick.
		ServerPlayer player = helper.getPlayer();
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);

		Slot planksSlot = helper.getStandardInventorySlots(menu).get(0);
		Slot sticksSlot = helper.getStandardInventorySlots(menu).get(1);
		planksSlot.set(new ItemStack(Items.OAK_PLANKS, 2));
		sticksSlot.set(new ItemStack(Items.STICK, 1));

		List<Slot> craftingSlots = menu.getInputGridSlots();
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
				CraftingMenu::getInputGridSlots,
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

	@GameTest
	@EmptyTemplate
	@TestHolder(
		description = "Rolls back partial extra transfers for counted ingredients when complete sets are required."
	)
	public static void rollsBackIncompleteCountedCompleteSet(RecipeTransferTestHelper helper) {
		// Setup: max transfer can start a second counted plank set, but not a second stick.
		ServerPlayer player = helper.getPlayer();
		CraftingMenu menu = helper.openMenu(CraftingMenu::new);

		Slot planksSlot = helper.getStandardInventorySlots(menu).get(0);
		Slot sticksSlot = helper.getStandardInventorySlots(menu).get(1);
		planksSlot.set(new ItemStack(Items.OAK_PLANKS, 5));
		sticksSlot.set(new ItemStack(Items.STICK, 1));

		List<Slot> craftingSlots = menu.getInputGridSlots();
		List<Slot> inventorySlots = helper.getStandardInventorySlots(menu);
		List<TransferOperation> operations = List.of(
			new TransferOperation(planksSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_LEFT).index, 2),
			new TransferOperation(sticksSlot.index, craftingSlots.get(CRAFTING_GRID_TOP_CENTER).index)
		);

		// Operation: call the server transfer directly with counted operations and complete-set rollback.
		BasicRecipeTransferHandlerServer.setItems(
			player,
			operations,
			craftingSlots,
			inventorySlots,
			true,
			true
		);

		// Assertions: only one complete set is transferred, and the extra counted plank attempt is restored.
		helper.createMenuChecker(menu)
			.assertResults(
				RecipeTransferGameTests::getCraftingResultSlots,
				List.of()
			)
			.assertCraftingArea(
				CraftingMenu::getInputGridSlots,
				List.of(
					stackAt(CRAFTING_GRID_TOP_LEFT, new ItemStack(Items.OAK_PLANKS, 2)),
					stackAt(CRAFTING_GRID_TOP_CENTER, Items.STICK)
				)
			)
			.assertPlayerInventory(
				List.of(stackAt(0, new ItemStack(Items.OAK_PLANKS, 3)))
			)
			.assertAllSlotsChecked();
		helper.succeed();
	}

	private static TransferRecipe<RecipeHolder<CraftingRecipe>> craftingTableRecipe() {
		return craftingRecipe("crafting_table", new ItemStack(Items.CRAFTING_TABLE), grid(
			ingredient(CRAFTING_GRID_TOP_LEFT, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_TOP_CENTER, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_MIDDLE_LEFT, Items.OAK_PLANKS),
			ingredient(CRAFTING_GRID_CENTER, Items.OAK_PLANKS)
		));
	}

	private static TransferRecipe<RecipeHolder<CraftingRecipe>> stackedPlanksRecipe(String idPath, int count) {
		return craftingRecipe(idPath, new ItemStack(Items.STICK), grid(
			ingredient(CRAFTING_GRID_TOP_LEFT, TestRecipeSlotView.item(new ItemStack(Items.OAK_PLANKS, count)))
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
		ContainerLevelAccess access = ContainerLevelAccess.create(inventory.player.level(), inventory.player.blockPosition());
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

	private static Slot rejectingSlot(Slot slot) {
		return new Slot(slot.container, slot.getContainerSlot(), slot.x, slot.y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		};
	}

	private static List<Slot> getCraftingResultSlots(AbstractCraftingMenu menu) {
		return List.of(menu.getResultSlot());
	}

	private static List<Slot> getPlayerInventorySlots(InventoryMenu menu) {
		List<Slot> inventorySlots = new ArrayList<>();
		inventorySlots.addAll(menu.slots.subList(InventoryMenu.ARMOR_SLOT_END, InventoryMenu.SHIELD_SLOT));
		inventorySlots.addAll(menu.slots.subList(InventoryMenu.ARMOR_SLOT_START, InventoryMenu.ARMOR_SLOT_END));
		inventorySlots.add(menu.getSlot(InventoryMenu.SHIELD_SLOT));
		return inventorySlots;
	}

	private static List<Slot> getCrafterSlots(CrafterMenu menu) {
		return menu.slots.stream()
			.filter(slot -> slot.container == menu.getContainer())
			.toList();
	}

	private static List<Slot> getCrafterResultSlots(CrafterMenu menu) {
		return List.of(menu.getSlot(CRAFTER_RESULT_SLOT));
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
		return List.of(
			menu.getSlot(SmithingMenu.TEMPLATE_SLOT),
			menu.getSlot(SmithingMenu.BASE_SLOT),
			menu.getSlot(SmithingMenu.ADDITIONAL_SLOT)
		);
	}

	private static List<Slot> getSmithingResultSlots(SmithingMenu menu) {
		return List.of(menu.getSlot(SmithingMenu.RESULT_SLOT));
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

	private static ItemStack lingeringPotion(Holder<Potion> potion) {
		ItemStack stack = new ItemStack(Items.LINGERING_POTION);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}

	private static ItemStack tippedArrow(Holder<Potion> potion, int count) {
		ItemStack stack = new ItemStack(Items.TIPPED_ARROW, count);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
		return stack;
	}

}
