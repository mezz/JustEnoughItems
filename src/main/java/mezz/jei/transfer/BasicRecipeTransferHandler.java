package mezz.jei.transfer;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.ServerInfo;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.util.StringUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

public class BasicRecipeTransferHandler<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<C, R> transferInfo;

	public BasicRecipeTransferHandler(IStackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper, IRecipeTransferInfo<C, R> transferInfo) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.transferInfo = transferInfo;
	}

	@Override
	public Class<C> getContainerClass() {
		return transferInfo.getContainerClass();
	}

	@Override
	public Class<R> getRecipeClass() {
		return transferInfo.getRecipeClass();
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(C container, R recipe, IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!ServerInfo.isJeiOnServer()) {
			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		if (!transferInfo.canHandle(container, recipe)) {
			return handlerHelper.createInternalError();
		}

		Collection<Slot> craftingSlots = Collections.unmodifiableCollection(transferInfo.getRecipeSlots(container, recipe));
		Collection<Slot> inventorySlots = Collections.unmodifiableCollection(transferInfo.getInventorySlots(container, recipe));
		Collection<Integer> craftingSlotIndexes = slotIndexes(craftingSlots);
		Collection<Integer> inventorySlotIndexes = slotIndexes(inventorySlots);

		if (!validateTransferInfo(transferInfo, container, craftingSlotIndexes, inventorySlotIndexes)) {
			return handlerHelper.createInternalError();
		}

		Collection<IRecipeSlotView> inputItemSlotViews = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT, VanillaTypes.ITEM);
		if (!validateRecipeView(transferInfo, container, craftingSlotIndexes, inputItemSlotViews)) {
			return handlerHelper.createInternalError();
		}

		InventoryState inventoryState = getInventoryState(craftingSlots, inventorySlots, player, container, transferInfo);
		if (inventoryState == null) {
			return handlerHelper.createInternalError();
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		int inputCount = inputItemSlotViews.size();
		if (!inventoryState.hasRoom(inputCount)) {
			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

		RecipeTransferUtil.MatchingItemsResult matchingItemsResult = RecipeTransferUtil.getMatchingItems(stackHelper, inventoryState.availableItemStacks, inputItemSlotViews);

		if (matchingItemsResult.missingItems.size() > 0) {
			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.missing");
			return handlerHelper.createUserErrorForMissingSlots(message, matchingItemsResult.missingItems);
		}

		{
			Set<Slot> recipeTargets = matchingItemsResult.matchingItems.keySet().stream()
				.map(IRecipeSlotView::getContainerSlotIndex)
				.flatMapToInt(OptionalInt::stream)
				.mapToObj(container::getSlot)
				.collect(Collectors.toSet());

			Collection<Slot> ingredientTargets = matchingItemsResult.matchingItems.values();
			if (!RecipeTransferUtil.validateSlots(player, recipeTargets, ingredientTargets, craftingSlots, inventorySlots)) {
				return handlerHelper.createInternalError();
			}
		}

		if (doTransfer) {
			boolean requireCompleteSets = transferInfo.requireCompleteSets(container, recipe);
			PacketRecipeTransfer packet = new PacketRecipeTransfer(
				matchingItemsResult.matchingItems,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets
			);
			Network.sendPacketToServer(packet);
		}

		return null;
	}

	public static <C extends AbstractContainerMenu, R> boolean validateTransferInfo(
		IRecipeTransferInfo<C, R> transferInfo,
		C container,
		Collection<Integer> craftingSlotIndexes,
		Collection<Integer> inventorySlotIndexes
	) {
		Collection<Integer> containerSlotIndexes = slotIndexes(container.slots);

		if (!containerSlotIndexes.containsAll(craftingSlotIndexes)) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
					"The Recipes Transfer Helper references crafting slot indexes [{}] that are not found in the inventory container slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(craftingSlotIndexes), StringUtil.intsToString(containerSlotIndexes)
			);
			return false;
		}

		if (!containerSlotIndexes.containsAll(inventorySlotIndexes)) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
					"The Recipes Transfer Helper references inventory slot indexes [{}] that are not found in the inventory container slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(inventorySlotIndexes), StringUtil.intsToString(containerSlotIndexes)
			);
			return false;
		}

		return true;
	}

	public static <C extends AbstractContainerMenu, R> boolean validateRecipeView(
		IRecipeTransferInfo<C, R> transferInfo,
		C container,
		Collection<Integer> craftingSlotIndexes,
		Collection<IRecipeSlotView> inputSlots
	) {
		Collection<Integer> inputSlotIndexes = recipeSlotIndexes(inputSlots);
		if (!craftingSlotIndexes.containsAll(inputSlotIndexes)) {
			LOGGER.error("Recipe View {} does not work for container {}. " +
					"The Recipe View references input slot indexes [{}] that are not found in the inventory crafting slots [{}]",
				transferInfo.getClass(), container.getClass(), StringUtil.intsToString(inputSlotIndexes), StringUtil.intsToString(craftingSlotIndexes)
			);
			return false;
		}

		return true;
	}

	public static Collection<Integer> slotIndexes(Collection<Slot> slots) {
		return slots.stream()
			.map(s -> s.index)
			.sorted()
			.distinct()
			.toList();
	}

	public static Collection<Integer> recipeSlotIndexes(Collection<IRecipeSlotView> recipeSlotViews) {
		return recipeSlotViews.stream()
			.map(IRecipeSlotView::getContainerSlotIndex)
			.flatMapToInt(OptionalInt::stream)
			.sorted()
			.distinct()
			.boxed()
			.toList();
	}

	@Nullable
	public static <C extends AbstractContainerMenu, R> InventoryState getInventoryState(
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots,
		Player player,
		C container,
		IRecipeTransferInfo<C, R> transferInfo
	) {
		Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (!slot.mayPickup(player)) {
					LOGGER.error(
						"Recipe Transfer helper {} does not work for container {}. " +
							"The Player is not able to move items out of Crafting Slot number {}",
						transferInfo.getClass(), container.getClass(), slot.index
					);
					return null;
				}
				filledCraftSlotCount++;
				availableItemStacks.put(slot, stack.copy());
			}
		}

		for (Slot slot : inventorySlots) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (!slot.mayPickup(player)) {
					LOGGER.error(
						"Recipe Transfer helper {} does not work for container {}. " +
							"The Player is not able to move items out of Inventory Slot number {}",
						transferInfo.getClass(), container.getClass(), slot.index
					);
					return null;
				}
				availableItemStacks.put(slot, stack.copy());
			} else {
				emptySlotCount++;
			}
		}

		return new InventoryState(availableItemStacks, filledCraftSlotCount, emptySlotCount);
	}

	public record InventoryState(
		Map<Slot, ItemStack> availableItemStacks,
		int filledCraftSlotCount,
		int emptySlotCount
	) {
		/**
		 * check if we have enough inventory space to shuffle items around to their final locations
		 */
		public boolean hasRoom(int inputCount) {
			return filledCraftSlotCount - inputCount <= emptySlotCount;
		}
	}
}
