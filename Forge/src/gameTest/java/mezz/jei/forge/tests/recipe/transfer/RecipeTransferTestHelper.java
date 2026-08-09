package mezz.jei.forge.tests.recipe.transfer;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.forge.tests.lib.JeiGameTestHelper;
import mezz.jei.forge.tests.lib.TargetSlots;
import mezz.jei.forge.tests.lib.TestConnectionToServer;
import mezz.jei.forge.tests.lib.TestRecipeSlotView;
import mezz.jei.forge.tests.lib.TransferRecipe;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RecipeTransferTestHelper extends JeiGameTestHelper {
	public RecipeTransferTestHelper(GameTestHelper helper) {
		super(helper);
	}

	public <M extends AbstractContainerMenu> TransferResult<M> transfer(
		RecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu
	) {
		return transfer(recipeType, recipe, menu, false);
	}

	public <M extends AbstractContainerMenu> TransferResult<M> transfer(
		RecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu,
		boolean maxTransfer
	) {
		return transfer(recipeType, recipe, menu, maxTransfer, createServerConnection());
	}

	public <M extends AbstractContainerMenu> TransferResult<M> transfer(
		RecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu,
		boolean maxTransfer,
		TestConnectionToServer serverConnection
	) {
		ServerPlayer player = getPlayer();
		if (player.containerMenu != menu) {
			throw createFailException("Expected the transfer menu to be the player's open menu");
		}
		List<ItemStack> initialInventoryStacks = copyInventoryStacks(menu, player);
		return executeTransfer(recipeType, recipe, menu, initialInventoryStacks, maxTransfer, serverConnection);
	}

	private <M extends AbstractContainerMenu> TransferResult<M> executeTransfer(
		RecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu,
		List<ItemStack> initialInventoryStacks,
		boolean maxTransfer,
		TestConnectionToServer serverConnection
	) {
		ServerPlayer player = getPlayer();
		TestRecipeTransferRegistration transferRegistration = createTransferRegistration(serverConnection);
		IRecipeTransferHandler<M, Object> transferHandler = transferRegistration.getTransferHandler(menu, recipeType);
		IRecipeTransferError transferError = runWithServerConnection(
			serverConnection,
			connection -> transferHandler.transferRecipe(menu, recipe.recipe(), recipe.slotsView(), player, maxTransfer, true)
		);

		return new TransferResult<>(player, menu, recipe, initialInventoryStacks, transferRegistration.getTransferHelper(), transferError);
	}

	public void assertTransferSucceeded(TransferResult<?> result) {
		if (result.transferError() != null) {
			throw createFailException("Expected recipe transfer to succeed, got %s".formatted(result.transferError().getType()));
		}
	}

	public void assertTransferError(TransferResult<?> result, Class<? extends IRecipeTransferError> expectedErrorClass) {
		if (result.transferError() == null || result.transferError().getClass() != expectedErrorClass) {
			String actualError = result.transferError() == null ? "success" : result.transferError().getClass().getSimpleName();
			throw createFailException("Expected recipe transfer to report %s, got %s".formatted(expectedErrorClass.getSimpleName(), actualError));
		}
	}

	public <M extends AbstractContainerMenu> void assertSuccessfulTransfer(TransferResult<M> result, TargetSlots<M> targetSlots) {
		assertTransferSucceeded(result);
		List<Slot> actualTargetSlots = targetSlots.get(result.menu(), result.player());
		assertRecipeTransferred(actualTargetSlots, result.recipe(), result.menu());
		// Reconcile every inventory slot against the items placed in the recipe slots.
		// This catches transfers that duplicate items into any previously empty inventory slot.
		assertInventoryUpdated(result.menu(), result.initialInventoryStacks(), actualTargetSlots);
	}

	public <M extends AbstractContainerMenu> void assertFailedTransfer(
		TransferResult<M> result,
		TargetSlots<M> targetSlots,
		Class<? extends IRecipeTransferError> expectedErrorClass
	) {
		assertTransferError(result, expectedErrorClass);

		List<Slot> actualTargetSlots = targetSlots.get(result.menu(), result.player());
		for (Slot targetSlot : actualTargetSlots) {
			assertEmptySlot(targetSlot);
		}
		assertInventoryRestored(result.menu(), result.initialInventoryStacks());
	}

	public void assertRecipeHasIngredientsOutsidePlayerGrid(TransferResult<?> result) {
		getCraftingRecipe(result.recipe().recipe());
		if (!(result.menu() instanceof RecipeBookMenu<?> craftingMenu)) {
			throw createFailException("Expected %s to be a crafting menu".formatted(result.menu()));
		}
		List<Integer> playerGridIndexes = gridIndexes(result.recipe(), craftingMenu.getGridWidth(), craftingMenu.getGridHeight());
		List<TestRecipeSlotView> inputSlots = result.recipe().inputSlots();
		boolean hasIngredientOutsidePlayerGrid = false;
		for (int i = 0; i < inputSlots.size(); i++) {
			if (!inputSlots.get(i).isEmpty() && !playerGridIndexes.contains(i)) {
				hasIngredientOutsidePlayerGrid = true;
				break;
			}
		}
		if (!hasIngredientOutsidePlayerGrid) {
			throw createFailException("Expected crafting recipe %s to have ingredients outside the player 2x2 grid".formatted(
				result.recipe().recipe()
			));
		}
	}

	public TestConnectionToServer createConnectionWithoutCountedTransferPacket() {
		return new TestConnectionToServer(false);
	}

	private static TestRecipeTransferRegistration createTransferRegistration(TestConnectionToServer serverConnection) {
		TestRecipeTransferRegistration transferRegistration = new TestRecipeTransferRegistration(serverConnection);
		new VanillaPlugin().registerRecipeTransferHandlers(transferRegistration);
		return transferRegistration;
	}

	private static List<ItemStack> copyInventoryStacks(AbstractContainerMenu menu, Player player) {
		return JeiGameTestHelper.getStandardInventorySlots(menu, player)
			.stream()
			.map(slot -> slot.getItem().copy())
			.toList();
	}

	private void assertRecipeTransferred(
		List<Slot> targetSlots,
		TransferRecipe<?> recipe,
		AbstractContainerMenu menu
	) {
		List<TestRecipeSlotView> expectedSlots = expectedSlotsForTargets(recipe, menu);
		if (expectedSlots.size() != targetSlots.size()) {
			throw createFailException("Expected %s recipe target slots, got %s".formatted(expectedSlots.size(), targetSlots.size()));
		}

		for (int i = 0; i < targetSlots.size(); i++) {
			TestRecipeSlotView expectedSlot = expectedSlots.get(i);
			Slot targetSlot = targetSlots.get(i);
			if (expectedSlot.isEmpty()) {
				assertEmptySlot(targetSlot);
			} else if (!expectedSlot.matches(targetSlot.getItem())) {
				throw createFailException("Expected slot %s to contain one of %s, got %s".formatted(
					targetSlot.index,
					expectedSlot.describeItems(),
					targetSlot.getItem()
				));
			} else if (targetSlot.getItem().getCount() != getExpectedCount(expectedSlot, targetSlot.getItem())) {
				throw createFailException("Expected slot %s to contain the recipe ingredient count, got %s".formatted(
					targetSlot.index,
					targetSlot.getItem()
				));
			}
		}
	}

	private static int getExpectedCount(TestRecipeSlotView expectedSlot, ItemStack actualStack) {
		return expectedSlot.itemStacks()
			.stream()
			.filter(stack -> ItemStack.isSameItemSameTags(stack, actualStack))
			.mapToInt(ItemStack::getCount)
			.max()
			.orElse(1);
	}

	private List<TestRecipeSlotView> expectedSlotsForTargets(
		TransferRecipe<?> recipe,
		AbstractContainerMenu menu
	) {
		if (
			menu instanceof RecipeBookMenu<?> craftingMenu &&
			recipe.recipe() instanceof CraftingRecipe craftingRecipe
		) {
			return expectedCraftingGridSlots(recipe, craftingMenu);
		}
		return recipe.inputSlots();
	}

	private List<TestRecipeSlotView> expectedCraftingGridSlots(
		TransferRecipe<?> recipe,
		RecipeBookMenu<?> craftingMenu
	) {
		List<Integer> craftingGridIndexes = gridIndexes(recipe, craftingMenu.getGridWidth(), craftingMenu.getGridHeight());
		List<TestRecipeSlotView> expectedSlots = new ArrayList<>(craftingGridIndexes.size());
		for (int guiSlotIndex : craftingGridIndexes) {
			expectedSlots.add(recipe.inputSlots().get(guiSlotIndex));
		}
		return expectedSlots;
	}

	private List<Integer> gridIndexes(TransferRecipe<?> recipe, int targetGridWidth, int targetGridHeight) {
		int recipeGridWidth = squareGridWidth(recipe.inputSlots().size());
		List<Integer> indexes = new ArrayList<>(targetGridWidth * targetGridHeight);
		for (int row = 0; row < targetGridHeight; row++) {
			for (int column = 0; column < targetGridWidth; column++) {
				indexes.add(row * recipeGridWidth + column);
			}
		}
		return indexes;
	}

	private int squareGridWidth(int slotCount) {
		int gridWidth = (int) Math.sqrt(slotCount);
		if (gridWidth * gridWidth != slotCount) {
			throw createFailException("Expected crafting recipe slot count %s to form a square grid".formatted(slotCount));
		}
		return gridWidth;
	}

	private CraftingRecipe getCraftingRecipe(Object recipe) {
		if (recipe instanceof CraftingRecipe craftingRecipe) {
			return craftingRecipe;
		}
		throw createFailException("Expected %s to be a crafting recipe".formatted(recipe));
	}

	private void assertInventoryRestored(AbstractContainerMenu menu, List<ItemStack> initialInventoryStacks) {
		List<Slot> inventorySlots = getStandardInventorySlots(menu);
		if (inventorySlots.size() != initialInventoryStacks.size()) {
			throw createFailException("Expected %s inventory slots, got %s".formatted(initialInventoryStacks.size(), inventorySlots.size()));
		}

		for (int i = 0; i < inventorySlots.size(); i++) {
			assertSlot(inventorySlots.get(i), initialInventoryStacks.get(i));
		}
	}

	private void assertInventoryUpdated(AbstractContainerMenu menu, List<ItemStack> initialInventoryStacks, List<Slot> targetSlots) {
		List<Slot> inventorySlots = getStandardInventorySlots(menu);
		if (inventorySlots.size() != initialInventoryStacks.size()) {
			throw createFailException("Expected %s inventory slots, got %s".formatted(initialInventoryStacks.size(), inventorySlots.size()));
		}

		List<Integer> expectedCounts = initialInventoryStacks.stream()
			.map(ItemStack::getCount)
			.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

		// Treat each item placed in a target slot as having been removed from the captured inventory.
		for (Slot targetSlot : targetSlots) {
			ItemStack targetStack = targetSlot.getItem();
			for (int i = 0; i < targetStack.getCount(); i++) {
				int sourceIndex = findInventoryStack(initialInventoryStacks, expectedCounts, targetStack);
				if (sourceIndex < 0) {
					throw createFailException("Transferred unexpected stack %s into slot %s".formatted(targetStack, targetSlot.index));
				}
				expectedCounts.set(sourceIndex, expectedCounts.get(sourceIndex) - 1);
			}
		}

		// Every current inventory slot must now match the remaining expected count.
		for (int i = 0; i < inventorySlots.size(); i++) {
			Slot inventorySlot = inventorySlots.get(i);
			ItemStack initialInventoryStack = initialInventoryStacks.get(i);
			int expectedCount = expectedCounts.get(i);
			if (expectedCount == 0) {
				assertEmptySlot(inventorySlot);
			} else {
				ItemStack expectedStack = initialInventoryStack.copy();
				expectedStack.setCount(expectedCount);
				assertSlot(inventorySlot, expectedStack);
			}
		}
	}

	private static int findInventoryStack(List<ItemStack> inventoryStacks, List<Integer> expectedCounts, ItemStack transferredStack) {
		for (int i = 0; i < inventoryStacks.size(); i++) {
			if (expectedCounts.get(i) > 0 && ItemStack.isSameItemSameTags(inventoryStacks.get(i), transferredStack)) {
				return i;
			}
		}
		return -1;
	}

	public record TransferResult<M extends AbstractContainerMenu>(
		ServerPlayer player,
		M menu,
		TransferRecipe<?> recipe,
		List<ItemStack> initialInventoryStacks,
		IRecipeTransferHandlerHelper handlerHelper,
		@Nullable IRecipeTransferError transferError
	) {
	}
}
