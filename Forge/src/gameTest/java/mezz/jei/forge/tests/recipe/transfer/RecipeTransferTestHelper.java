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
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		List<ItemStack> initialMenuStacks = copyMenuStacks(menu);
		return executeTransfer(recipeType, recipe, menu, initialInventoryStacks, initialMenuStacks, maxTransfer, serverConnection);
	}

	private <M extends AbstractContainerMenu> TransferResult<M> executeTransfer(
		RecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu,
		List<ItemStack> initialInventoryStacks,
		List<ItemStack> initialMenuStacks,
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

		return new TransferResult<>(
			player,
			menu,
			recipe,
			initialInventoryStacks,
			initialMenuStacks,
			transferRegistration.getTransferHelper(),
			transferError
		);
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
		assertRecipeTransferred(actualTargetSlots, result.recipe(), result.menu(), result.handlerHelper());
		assertMenuItemsConserved(result.menu(), result.initialMenuStacks(), actualTargetSlots);
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
		CraftingRecipe craftingRecipe = getCraftingRecipe(result.recipe().recipe());
		if (!(result.menu() instanceof RecipeBookMenu<?> craftingMenu)) {
			throw createFailException("Expected %s to be a crafting menu".formatted(result.menu()));
		}
		List<Integer> playerGridIndexes = gridIndexes(result.recipe(), craftingMenu.getGridWidth(), craftingMenu.getGridHeight());
		boolean hasIngredientOutsidePlayerGrid = result.handlerHelper().getGuiSlotIndexToIngredientMap(craftingRecipe)
			.keySet()
			.stream()
			.anyMatch(slotIndex -> !playerGridIndexes.contains(slotIndex));
		if (!hasIngredientOutsidePlayerGrid) {
			throw createFailException("Expected crafting recipe %s to have ingredients outside the player 2x2 grid".formatted(
				craftingRecipe
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

	private static List<ItemStack> copyMenuStacks(AbstractContainerMenu menu) {
		return menu.slots.stream()
			.map(slot -> slot.getItem().copy())
			.toList();
	}

	private void assertRecipeTransferred(
		List<Slot> targetSlots,
		TransferRecipe<?> recipe,
		AbstractContainerMenu menu,
		IRecipeTransferHandlerHelper handlerHelper
	) {
		List<TestRecipeSlotView> expectedSlots = expectedSlotsForTargets(recipe, menu, handlerHelper);
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
		AbstractContainerMenu menu,
		IRecipeTransferHandlerHelper handlerHelper
	) {
		if (
			menu instanceof RecipeBookMenu<?> craftingMenu &&
			recipe.recipe() instanceof CraftingRecipe craftingRecipe
		) {
			return expectedCraftingGridSlots(recipe, craftingRecipe, craftingMenu, handlerHelper);
		}
		return recipe.inputSlots();
	}

	private List<TestRecipeSlotView> expectedCraftingGridSlots(
		TransferRecipe<?> recipe,
		CraftingRecipe craftingRecipe,
		RecipeBookMenu<?> craftingMenu,
		IRecipeTransferHandlerHelper handlerHelper
	) {
		Map<Integer, Ingredient> guiSlotIndexToIngredientMap = handlerHelper.getGuiSlotIndexToIngredientMap(craftingRecipe);
		if (guiSlotIndexToIngredientMap.isEmpty()) {
			throw createFailException("Expected crafting recipe %s to have mapped GUI ingredients".formatted(craftingRecipe.getId()));
		}

		List<Integer> craftingGridIndexes = gridIndexes(recipe, craftingMenu.getGridWidth(), craftingMenu.getGridHeight());
		List<TestRecipeSlotView> expectedSlots = new ArrayList<>(craftingGridIndexes.size());
		for (int guiSlotIndex : craftingGridIndexes) {
			if (guiSlotIndexToIngredientMap.containsKey(guiSlotIndex)) {
				expectedSlots.add(recipe.inputSlots().get(guiSlotIndex));
			} else {
				expectedSlots.add(TestRecipeSlotView.empty());
			}
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

	private void assertMenuItemsConserved(AbstractContainerMenu menu, List<ItemStack> initialMenuStacks, List<Slot> targetSlots) {
		if (menu.slots.size() != initialMenuStacks.size()) {
			throw createFailException("Expected %s menu slots, got %s".formatted(initialMenuStacks.size(), menu.slots.size()));
		}

		List<Slot> relevantSlots = new ArrayList<>(getStandardInventorySlots(menu));
		relevantSlots.addAll(targetSlots);
		List<ItemStack> remainingInitialStacks = relevantSlots.stream()
			.map(slot -> initialMenuStacks.get(slot.index).copy())
			.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

		for (Slot slot : relevantSlots) {

			ItemStack unexplainedStack = slot.getItem().copy();
			for (ItemStack initialStack : remainingInitialStacks) {
				if (unexplainedStack.isEmpty()) {
					break;
				}
				if (ItemStack.isSameItemSameTags(initialStack, unexplainedStack)) {
					int matchedCount = Math.min(initialStack.getCount(), unexplainedStack.getCount());
					initialStack.shrink(matchedCount);
					unexplainedStack.shrink(matchedCount);
				}
			}
			if (!unexplainedStack.isEmpty()) {
				throw createFailException("Recipe transfer created an unexpected stack in slot %s: %s".formatted(
					slot.index,
					unexplainedStack
				));
			}
		}

		List<ItemStack> missingStacks = remainingInitialStacks.stream()
			.filter(stack -> !stack.isEmpty())
			.toList();
		if (!missingStacks.isEmpty()) {
			throw createFailException("Recipe transfer lost menu items: %s".formatted(missingStacks));
		}
	}

	public record TransferResult<M extends AbstractContainerMenu>(
		ServerPlayer player,
		M menu,
		TransferRecipe<?> recipe,
		List<ItemStack> initialInventoryStacks,
		List<ItemStack> initialMenuStacks,
		IRecipeTransferHandlerHelper handlerHelper,
		@Nullable IRecipeTransferError transferError
	) {
	}
}
