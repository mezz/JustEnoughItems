package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.ServerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PlayerRecipeTransferHandler implements IRecipeTransferHandler<InventoryMenu, CraftingRecipe> {
	/**
	 * Indexes from the crafting table that fit into the player crafting grid
	 * when we trim the right and bottom edges .
	 */
	private static final Int2IntMap INDEX_MAPPING = new Int2IntArrayMap(Map.of(
		1, 1,
		2, 2,
		4, 3,
		5, 4
	));

	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<InventoryMenu, CraftingRecipe> transferInfo;

	public PlayerRecipeTransferHandler(IStackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.transferInfo = new BasicRecipeTransferInfo<>(InventoryMenu.class, CraftingRecipe.class, VanillaRecipeCategoryUid.CRAFTING, 1, 4, 9, 36);
	}

	@Override
	public Class<InventoryMenu> getContainerClass() {
		return InventoryMenu.class;
	}

	@Override
	public Class<CraftingRecipe> getRecipeClass() {
		return CraftingRecipe.class;
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(InventoryMenu container, CraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!ServerInfo.isJeiOnServer()) {
			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}
		
		boolean allGoodInputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT, VanillaTypes.ITEM)
			.stream()
			.allMatch(slot -> INDEX_MAPPING.containsKey(slot.getSlotIndex()));

		if (!allGoodInputSlots) {
			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		// map the crafting table input slots to player inventory input slots
		recipeSlotsView = new RecipeSlotsView(mapSlots(recipeSlotsView.getSlotViews()));

		IRecipeTransferHandler<InventoryMenu, CraftingRecipe> handler = new BasicRecipeTransferHandler<>(stackHelper, handlerHelper, transferInfo);
		return handler.transferRecipe(container, recipe, recipeSlotsView, player, maxTransfer, doTransfer);

//		if (!ServerInfo.isJeiOnServer()) {
//			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.no.server");
//			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
//		}
//
//		if (!transferInfo.canHandle(container, recipe)) {
//			return handlerHelper.createInternalError();
//		}
//
//		List<Slot> craftingSlots = Collections.unmodifiableList(transferInfo.getRecipeSlots(container, recipe));
//		List<Slot> inventorySlots = Collections.unmodifiableList(transferInfo.getInventorySlots(container, recipe));
//		List<Integer> craftingSlotIndexes = BasicRecipeTransferHandler.slotIndexes(craftingSlots);
//		List<Integer> inventorySlotIndexes = BasicRecipeTransferHandler.slotIndexes(inventorySlots);
//
//		if (!BasicRecipeTransferHandler.validateTransferInfo(transferInfo, container, craftingSlotIndexes, inventorySlotIndexes)) {
//			return handlerHelper.createInternalError();
//		}
//
//		List<IRecipeSlotView> inputItemSlotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT, VanillaTypes.ITEM);
//		if (!BasicRecipeTransferHandler.validateRecipeView(transferInfo, container, craftingSlotIndexes, inputItemSlotViews)) {
//			return handlerHelper.createInternalError();
//		}
//
//		BasicRecipeTransferHandler.InventoryState inventoryState = BasicRecipeTransferHandler.getInventoryState(craftingSlots, inventorySlots, player, container, transferInfo);
//		if (inventoryState == null) {
//			return handlerHelper.createInternalError();
//		}
//
//		// check if we have enough inventory space to shuffle items around to their final locations
//		int inputCount = inputItemSlotViews.size();
//		if (!inventoryState.hasRoom(inputCount)) {
//			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.inventory.full");
//			return handlerHelper.createUserErrorWithTooltip(message);
//		}
//
//		RecipeTransferUtil.MatchingItemsResult matchingItemsResult = RecipeTransferUtil.getMatchingItems(stackHelper, inventoryState.availableItemStacks(), inputItemSlotViews);
//
//		if (matchingItemsResult.missingItems.size() > 0) {
//			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.missing");
//			// unmap the player inventory input slots back to crafting table slots,
//			// to display them correctly as missing in JEI's display of the crafting recipe
////			List<IRecipeSlotView> unmappedMissingItems = unmapSlots(matchingItemsResult.missingItems);
//			return handlerHelper.createUserErrorForMissingSlots(message, matchingItemsResult.missingItems);
//		}
//
//		if (doTransfer) {
//			boolean requireCompleteSets = transferInfo.requireCompleteSets(container, recipe);
//			PacketRecipeTransfer packet = new PacketRecipeTransfer(
//				matchingItemsResult.matchingItems,
//				craftingSlotIndexes,
//				inventorySlotIndexes,
//				maxTransfer,
//				requireCompleteSets
//			);
//			Network.sendPacketToServer(packet);
//		}
//
//		return null;
	}

	private static List<IRecipeSlotView> mapSlots(List<IRecipeSlotView> slots) {
		return slots.stream()
			.map(slot -> {
				int slotIndex = slot.getSlotIndex();
				int newSlotIndex = INDEX_MAPPING.getOrDefault(slotIndex, slotIndex);
				if (newSlotIndex == slotIndex) {
					return slot;
				}
				return new MappedRecipeSlotView(slot, newSlotIndex);
			})
			.toList();
	}

	private static List<IRecipeSlotView> unmapSlots(List<IRecipeSlotView> slots) {
		return slots.stream()
			.map(slot -> {
				if (slot instanceof MappedRecipeSlotView mappedSlot) {
					return mappedSlot.getOriginal();
				}
				return slot;
			})
			.toList();
	}

	private static class MappedRecipeSlotView implements IRecipeSlotView {
		private final IRecipeSlotView original;
		private final Integer newSlotIndex;

		public MappedRecipeSlotView(IRecipeSlotView original, Integer newSlotIndex) {
			this.original = original;
			this.newSlotIndex = newSlotIndex;
		}

		@Override
		public List<?> getAllIngredients() {
			return original.getAllIngredients();
		}

		@Override
		public <T> Stream<T> getAllIngredients(IIngredientType<T> ingredientType) {
			return original.getAllIngredients(ingredientType);
		}

		@Override
		public int getSlotIndex() {
			return newSlotIndex;
		}

		@Override
		public RecipeIngredientRole getRole() {
			return original.getRole();
		}

		@Override
		public void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset) {
			original.drawHighlight(stack, color, xOffset, yOffset);
		}

		public IRecipeSlotView getOriginal() {
			return original;
		}
	}
}
