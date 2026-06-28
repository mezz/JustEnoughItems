package mezz.jei.neoforge.tests.recipe.transfer;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.neoforge.tests.lib.MenuFactory;
import mezz.jei.neoforge.tests.lib.TargetSlots;
import mezz.jei.neoforge.tests.lib.TestConnectionToServer;
import mezz.jei.neoforge.tests.lib.TestRecipeSlotView;
import mezz.jei.neoforge.tests.lib.TransferRecipe;
import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecipeTransferTestHelper extends ExtendedGameTestHelper {
	@Nullable
	private ServerPlayer player;

	public RecipeTransferTestHelper(GameTestInfo info) {
		super(info);
	}

	@SuppressWarnings("removal")
	public ServerPlayer getPlayer() {
		if (player == null) {
			player = makeMockServerPlayerInLevel();
		}
		return player;
	}

	public GameTestException createFailException(String message) {
		return assertionException(Component.literal(message));
	}

	public <M extends AbstractContainerMenu> TransferResult<M> transferFromInventory(
		IRecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		MenuFactory<M> menuFactory,
		boolean maxTransfer,
		ItemStack... inventoryStacks
	) {
		ServerPlayer player = getPlayer();
		M menu = createMenu(menuFactory);
		List<InventorySlotState> sourceSlots = fillInventory(menu, player, List.of(inventoryStacks));
		return transfer(recipeType, recipe, menu, sourceSlots, maxTransfer);
	}

	public <M extends AbstractContainerMenu> TransferResult<M> transferFromMenu(
		IRecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu
	) {
		ServerPlayer player = getPlayer();
		player.containerMenu = menu;
		List<InventorySlotState> sourceSlots = captureInventorySlots(menu, player);
		return transfer(recipeType, recipe, menu, sourceSlots, false);
	}

	private <M extends AbstractContainerMenu> TransferResult<M> transfer(
		IRecipeType<?> recipeType,
		TransferRecipe<?> recipe,
		M menu,
		List<InventorySlotState> sourceSlots,
		boolean maxTransfer
	) {
		ServerPlayer player = getPlayer();
		TestConnectionToServer serverConnection = new TestConnectionToServer();
		TestRecipeTransferRegistration transferRegistration = createTransferRegistration(serverConnection);
		IRecipeTransferHandler<M, Object> transferHandler = transferRegistration.getTransferHandler(menu, recipeType);
		IRecipeTransferError transferError;
		try {
			serverConnection.setPlayer(player);
			transferError = transferHandler.transferRecipe(menu, recipe.recipe(), recipe.slotsView(), player, maxTransfer, true);
		} finally {
			serverConnection.clearPlayer();
		}

		return new TransferResult<>(player, menu, recipe, sourceSlots, transferRegistration.getTransferHelper(), transferError);
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
		assertInventoryUpdated(result.sourceSlots(), actualTargetSlots);
	}

	public <M extends AbstractContainerMenu> void assertFailedTransfer(TransferResult<M> result, TargetSlots<M> targetSlots, Class<? extends IRecipeTransferError> expectedErrorClass) {
		assertTransferError(result, expectedErrorClass);

		for (Slot targetSlot : targetSlots.get(result.menu(), result.player())) {
			assertEmptySlot(targetSlot);
		}
		for (InventorySlotState sourceSlot : result.sourceSlots()) {
			assertSlot(sourceSlot.slot(), sourceSlot.initialStack());
		}
	}

	public void assertRecipeHasIngredientsOutsidePlayerGrid(TransferResult<?> result) {
		RecipeHolder<CraftingRecipe> craftingRecipeHolder = getCraftingRecipeHolder(result.recipe().recipe());
		if (!(result.menu() instanceof AbstractCraftingMenu craftingMenu)) {
			throw createFailException("Expected %s to be a crafting menu".formatted(result.menu()));
		}
		List<Integer> playerGridIndexes = gridIndexes(result.recipe(), craftingMenu.getGridWidth(), craftingMenu.getGridHeight());
		boolean hasIngredientOutsidePlayerGrid = result.handlerHelper().getGuiSlotIndexToIngredientMap(craftingRecipeHolder)
			.keySet()
			.stream()
			.anyMatch(slotIndex -> !playerGridIndexes.contains(slotIndex));
		if (!hasIngredientOutsidePlayerGrid) {
			throw createFailException("Expected crafting recipe %s to have ingredients outside the player 2x2 grid".formatted(craftingRecipeHolder.id().identifier()));
		}
	}

	public <M extends AbstractContainerMenu> M createMenu(MenuFactory<M> menuFactory) {
		ServerPlayer player = getPlayer();
		M menu = menuFactory.create(0, player.getInventory());
		player.containerMenu = menu;
		return menu;
	}

	public List<Slot> getStandardInventorySlots(AbstractContainerMenu menu) {
		return menu.slots.stream()
			.filter(slot -> slot.container == getPlayer().getInventory())
			.filter(slot -> slot.getContainerSlot() < Inventory.INVENTORY_SIZE)
			.toList();
	}

	public List<Slot> getCraftingGridSlots(AbstractCraftingMenu menu, Player player) {
		return menu.getInputGridSlots();
	}

	public List<Slot> getCrafterSlots(CrafterMenu menu, Player player) {
		return menu.slots.stream()
			.filter(slot -> slot.container == menu.getContainer())
			.toList();
	}

	public List<Slot> getFurnaceIngredientSlots(AbstractFurnaceMenu menu, Player player) {
		return List.of(menu.getSlot(AbstractFurnaceMenu.INGREDIENT_SLOT));
	}

	public List<Slot> getFurnaceFuelSlots(AbstractFurnaceMenu menu, Player player) {
		return List.of(menu.getSlot(AbstractFurnaceMenu.FUEL_SLOT));
	}

	public List<Slot> getBrewingRecipeSlots(BrewingStandMenu menu, Player player) {
		return menu.slots.stream()
			.filter(slot -> slot.container != player.getInventory())
			.limit(4)
			.toList();
	}

	public List<Slot> getAnvilSlots(AnvilMenu menu, Player player) {
		return List.of(
			menu.getSlot(AnvilMenu.INPUT_SLOT),
			menu.getSlot(AnvilMenu.ADDITIONAL_SLOT)
		);
	}

	public List<Slot> getSmithingSlots(SmithingMenu menu, Player player) {
		return List.of(
			menu.getSlot(SmithingMenu.TEMPLATE_SLOT),
			menu.getSlot(SmithingMenu.BASE_SLOT),
			menu.getSlot(SmithingMenu.ADDITIONAL_SLOT)
		);
	}

	public void assertSlot(Slot slot, Item item, int count) {
		ItemStack stack = slot.getItem();
		if (!stack.is(item) || stack.getCount() != count) {
			throw createFailException("Expected slot %s to contain %s x%s, got %s x%s".formatted(
				slot.index,
				item.getDescriptionId(),
				count,
				stack.getItem().getDescriptionId(),
				stack.getCount()
			));
		}
	}

	public void assertSlot(Slot slot, ItemStack expectedStack) {
		ItemStack stack = slot.getItem();
		if (!ItemStack.isSameItemSameComponents(stack, expectedStack) || stack.getCount() != expectedStack.getCount()) {
			throw createFailException("Expected slot %s to contain %s, got %s".formatted(
				slot.index,
				expectedStack,
				stack
			));
		}
	}

	public void assertEmptySlot(Slot slot) {
		ItemStack stack = slot.getItem();
		if (!stack.isEmpty()) {
			throw createFailException("Expected slot %s to be empty, got %s x%s".formatted(
				slot.index,
				stack.getItem().getDescriptionId(),
				stack.getCount()
			));
		}
	}

	private static TestRecipeTransferRegistration createTransferRegistration(TestConnectionToServer serverConnection) {
		TestRecipeTransferRegistration transferRegistration = new TestRecipeTransferRegistration(serverConnection);
		new VanillaPlugin().registerRecipeTransferHandlers(transferRegistration);
		return transferRegistration;
	}

	private static List<InventorySlotState> fillInventory(AbstractContainerMenu menu, Player player, List<ItemStack> inventoryStacks) {
		List<Slot> inventorySlots = getStandardInventorySlots(menu, player);
		List<InventorySlotState> sourceSlots = new ArrayList<>(inventoryStacks.size());
		for (int i = 0; i < inventoryStacks.size(); i++) {
			Slot sourceSlot = inventorySlots.get(i);
			ItemStack stack = inventoryStacks.get(i).copy();
			sourceSlot.set(stack);
			sourceSlots.add(new InventorySlotState(sourceSlot, stack.copy()));
		}
		return sourceSlots;
	}

	private static List<InventorySlotState> captureInventorySlots(AbstractContainerMenu menu, Player player) {
		return getStandardInventorySlots(menu, player)
			.stream()
			.map(slot -> new InventorySlotState(slot, slot.getItem().copy()))
			.toList();
	}

	private static List<Slot> getStandardInventorySlots(AbstractContainerMenu menu, Player player) {
		return menu.slots.stream()
			.filter(slot -> slot.container == player.getInventory())
			.filter(slot -> slot.getContainerSlot() < Inventory.INVENTORY_SIZE)
			.toList();
	}

	private void assertRecipeTransferred(List<Slot> targetSlots, TransferRecipe<?> recipe, AbstractContainerMenu menu, IRecipeTransferHandlerHelper handlerHelper) {
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
			} else if (targetSlot.getItem().getCount() != 1) {
				throw createFailException("Expected slot %s to contain exactly one recipe ingredient, got %s".formatted(
					targetSlot.index,
					targetSlot.getItem()
				));
			}
		}
	}

	private List<TestRecipeSlotView> expectedSlotsForTargets(TransferRecipe<?> recipe, AbstractContainerMenu menu, IRecipeTransferHandlerHelper handlerHelper) {
		if (menu instanceof AbstractCraftingMenu craftingMenu && recipe.recipe() instanceof RecipeHolder<?> recipeHolder && recipeHolder.value() instanceof CraftingRecipe) {
			@SuppressWarnings("unchecked")
			RecipeHolder<CraftingRecipe> craftingRecipeHolder = (RecipeHolder<CraftingRecipe>) recipeHolder;
			return expectedCraftingGridSlots(recipe, craftingRecipeHolder, craftingMenu, handlerHelper);
		}
		return recipe.inputSlots();
	}

	private List<TestRecipeSlotView> expectedCraftingGridSlots(TransferRecipe<?> recipe, RecipeHolder<CraftingRecipe> recipeHolder, AbstractCraftingMenu craftingMenu, IRecipeTransferHandlerHelper handlerHelper) {
		Map<Integer, SlotDisplay> guiSlotIndexToIngredientMap = handlerHelper.getGuiSlotIndexToIngredientMap(recipeHolder);
		if (guiSlotIndexToIngredientMap.isEmpty()) {
			throw createFailException("Expected crafting recipe %s to have mapped GUI ingredients".formatted(recipeHolder.id().identifier()));
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

	private RecipeHolder<CraftingRecipe> getCraftingRecipeHolder(Object recipe) {
		if (recipe instanceof RecipeHolder<?> recipeHolder && recipeHolder.value() instanceof CraftingRecipe) {
			@SuppressWarnings("unchecked")
			RecipeHolder<CraftingRecipe> craftingRecipeHolder = (RecipeHolder<CraftingRecipe>) recipeHolder;
			return craftingRecipeHolder;
		}
		throw createFailException("Expected %s to be a crafting recipe".formatted(recipe));
	}

	private void assertInventoryUpdated(List<InventorySlotState> sourceSlots, List<Slot> targetSlots) {
		List<Integer> expectedCounts = sourceSlots.stream()
			.map(sourceSlot -> sourceSlot.initialStack().getCount())
			.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

		for (Slot targetSlot : targetSlots) {
			ItemStack targetStack = targetSlot.getItem();
			for (int i = 0; i < targetStack.getCount(); i++) {
				int sourceIndex = findSourceSlot(sourceSlots, expectedCounts, targetStack);
				if (sourceIndex < 0) {
					throw createFailException("Transferred unexpected stack %s into slot %s".formatted(targetStack, targetSlot.index));
				}
				expectedCounts.set(sourceIndex, expectedCounts.get(sourceIndex) - 1);
			}
		}

		for (int i = 0; i < sourceSlots.size(); i++) {
			InventorySlotState sourceSlot = sourceSlots.get(i);
			int expectedCount = expectedCounts.get(i);
			if (expectedCount == 0) {
				assertEmptySlot(sourceSlot.slot());
			} else {
				ItemStack expectedStack = sourceSlot.initialStack().copyWithCount(expectedCount);
				assertSlot(sourceSlot.slot(), expectedStack);
			}
		}
	}

	private static int findSourceSlot(List<InventorySlotState> sourceSlots, List<Integer> expectedCounts, ItemStack transferredStack) {
		for (int i = 0; i < sourceSlots.size(); i++) {
			if (expectedCounts.get(i) > 0 && ItemStack.isSameItemSameComponents(sourceSlots.get(i).initialStack(), transferredStack)) {
				return i;
			}
		}
		return -1;
	}

	public record TransferResult<M extends AbstractContainerMenu>(
		ServerPlayer player,
		M menu,
		TransferRecipe<?> recipe,
		List<InventorySlotState> sourceSlots,
		IRecipeTransferHandlerHelper handlerHelper,
		@Nullable IRecipeTransferError transferError
	) {
	}

	public record InventorySlotState(Slot slot, ItemStack initialStack) {
	}
}
