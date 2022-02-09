package mezz.jei.transfer;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.ServerInfo;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketRecipeTransfer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BasicRecipeTransferHandler<C extends AbstractContainerMenu, R> implements IRecipeTransferHandler<C, R> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<C, R> transferHelper;

	public BasicRecipeTransferHandler(IStackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper, IRecipeTransferInfo<C, R> transferHelper) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<C> getContainerClass() {
		return transferHelper.getContainerClass();
	}

	@Override
	public Class<R> getRecipeClass() {
		return transferHelper.getRecipeClass();
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(C container, R recipe, IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!ServerInfo.isJeiOnServer()) {
			Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		if (!transferHelper.canHandle(container, recipe)) {
			return handlerHelper.createInternalError();
		}

		Map<Integer, Slot> inventorySlots = new HashMap<>();
		for (Slot slot : transferHelper.getInventorySlots(container, recipe)) {
			inventorySlots.put(slot.index, slot);
		}

		Map<Integer, Slot> craftingSlots = new HashMap<>();
		for (Slot slot : transferHelper.getRecipeSlots(container, recipe)) {
			craftingSlots.put(slot.index, slot);
		}

		int inputCount = 0;
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values()) {
			if (ingredient.isInput() && !ingredient.getAllIngredients().isEmpty()) {
				inputCount++;
			}
		}

		if (inputCount > craftingSlots.size()) {
			LOGGER.error("Recipe Transfer helper {} does not work for container {}. " +
				"{} ingredients are marked as inputs in IRecipeCategory#setRecipe, but there are only {} crafting slots defined for the recipe transfer helper.",
				transferHelper.getClass(), container.getClass(), inputCount, craftingSlots.size()
			);
			return handlerHelper.createInternalError();
		}

		Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots.values()) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				if (!slot.mayPickup(player)) {
					LOGGER.error("Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}", transferHelper.getClass(), container.getClass(), slot.index);
					return handlerHelper.createInternalError();
				}
				filledCraftSlotCount++;
				availableItemStacks.put(slot.index, stack.copy());
			}
		}

		for (Slot slot : inventorySlots.values()) {
			final ItemStack stack = slot.getItem();
			if (!stack.isEmpty()) {
				availableItemStacks.put(slot.index, stack.copy());
			} else {
				emptySlotCount++;
			}
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		if (filledCraftSlotCount - inputCount > emptySlotCount) {
			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

		RecipeTransferUtil.MatchingItemsResult matchingItemsResult = RecipeTransferUtil.getMatchingItems(stackHelper, availableItemStacks, itemStackGroup.getGuiIngredients());

		if (matchingItemsResult.missingItems.size() > 0) {
			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.missing");
			return handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
		}

		List<Integer> craftingSlotIndexes = new ArrayList<>(craftingSlots.keySet());
		Collections.sort(craftingSlotIndexes);

		List<Integer> inventorySlotIndexes = new ArrayList<>(inventorySlots.keySet());
		Collections.sort(inventorySlotIndexes);

		// check that the slots exist and can be altered
		for (Map.Entry<Integer, Integer> entry : matchingItemsResult.matchingItems.entrySet()) {
			int craftNumber = entry.getKey();
			int slotNumber = craftingSlotIndexes.get(craftNumber);
			if (slotNumber < 0 || slotNumber >= container.slots.size()) {
				LOGGER.error("Recipes Transfer Helper {} references slot {} outside of the inventory's size {}", transferHelper.getClass(), slotNumber, container.slots.size());
				return handlerHelper.createInternalError();
			}
		}

		if (doTransfer) {
			boolean requireCompleteSets = transferHelper.requireCompleteSets(container, recipe);
			PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes, maxTransfer, requireCompleteSets);
			Network.sendPacketToServer(packet);
		}

		return null;
	}
}
