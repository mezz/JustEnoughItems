package mezz.jei.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.SessionData;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.util.Log;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BasicRecipeTransferHandler<C extends Container> implements IRecipeTransferHandler<C> {
	@Nonnull
	private final IRecipeTransferInfo<C> transferHelper;

	public BasicRecipeTransferHandler(@Nonnull IRecipeTransferInfo<C> transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<C> getContainerClass() {
		return transferHelper.getContainerClass();
	}

	@Override
	public String getRecipeCategoryUid() {
		return transferHelper.getRecipeCategoryUid();
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(@Nonnull C container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		IRecipeTransferHandlerHelper handlerHelper = Internal.getHelpers().recipeTransferHandlerHelper();
		StackHelper stackHelper = Internal.getStackHelper();

		if (!SessionData.isJeiOnServer()) {
			String tooltipMessage = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.no.server");
			return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
		}

		Map<Integer, Slot> inventorySlots = new HashMap<Integer, Slot>();
		for (Slot slot : transferHelper.getInventorySlots(container)) {
			inventorySlots.put(slot.slotNumber, slot);
		}

		Map<Integer, Slot> craftingSlots = new HashMap<Integer, Slot>();
		for (Slot slot : transferHelper.getRecipeSlots(container)) {
			craftingSlots.put(slot.slotNumber, slot);
		}

		int inputCount = 0;
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values()) {
			if (ingredient.isInput() && !ingredient.getAllIngredients().isEmpty()) {
				inputCount++;
			}
		}

		if (inputCount > craftingSlots.size()) {
			Log.error("Recipe Transfer helper {} does not work for container {}", transferHelper.getClass(), container.getClass());
			return handlerHelper.createInternalError();
		}

		Map<Integer, ItemStack> availableItemStacks = new HashMap<Integer, ItemStack>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots.values()) {
			if (slot.getHasStack()) {
				if (!slot.canTakeStack(player)) {
					Log.error("Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}", transferHelper.getClass(), container.getClass(), slot.slotNumber);
					return handlerHelper.createInternalError();
				}
				filledCraftSlotCount++;
				availableItemStacks.put(slot.slotNumber, slot.getStack().copy());
			}
		}

		for (Slot slot : inventorySlots.values()) {
			if (slot.getHasStack()) {
				availableItemStacks.put(slot.slotNumber, slot.getStack().copy());
			} else {
				emptySlotCount++;
			}
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		if (filledCraftSlotCount - inputCount > emptySlotCount) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

		StackHelper.MatchingItemsResult matchingItemsResult = stackHelper.getMatchingItems(availableItemStacks, itemStackGroup.getGuiIngredients());

		if (matchingItemsResult.missingItems.size() > 0) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
			return handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
		}

		List<Integer> craftingSlotIndexes = new ArrayList<Integer>(craftingSlots.keySet());
		Collections.sort(craftingSlotIndexes);

		List<Integer> inventorySlotIndexes = new ArrayList<Integer>(inventorySlots.keySet());
		Collections.sort(inventorySlotIndexes);

		// check that the slots exist and can be altered
		for (Map.Entry<Integer, Integer> entry : matchingItemsResult.matchingItems.entrySet()) {
			int craftNumber = entry.getKey();
			int slotNumber = craftingSlotIndexes.get(craftNumber);
			if (slotNumber >= container.inventorySlots.size()) {
				Log.error("Recipes Transfer Helper {} references slot {} outside of the inventory's size {}", transferHelper.getClass(), slotNumber, container.inventorySlots.size());
				return handlerHelper.createInternalError();
			}
			Slot slot = container.getSlot(slotNumber);
			ItemStack stack = container.getSlot(entry.getValue()).getStack();
			if (slot == null) {
				Log.error("The slot number {} does not exist in the container.", slotNumber);
				return handlerHelper.createInternalError();
			}
			if (!slot.isItemValid(stack)) {
				Log.error("The ItemStack {} is not valid for the slot number {}", stack, slotNumber);
				return handlerHelper.createInternalError();
			}
		}

		if (doTransfer) {
			PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes, maxTransfer);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		}

		return null;
	}
}
