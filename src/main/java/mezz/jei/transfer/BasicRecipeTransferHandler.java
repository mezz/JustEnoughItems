package mezz.jei.transfer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.Config;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.util.Log;
import mezz.jei.util.StackHelper;
import mezz.jei.util.Translator;

public class BasicRecipeTransferHandler implements IRecipeTransferHandler {
	@Nonnull
	private final IRecipeTransferInfo transferHelper;

	public BasicRecipeTransferHandler(@Nonnull IRecipeTransferInfo transferHelper) {
		this.transferHelper = transferHelper;
	}

	@Override
	public Class<? extends Container> getContainerClass() {
		return transferHelper.getContainerClass();
	}

	@Override
	public String getRecipeCategoryUid() {
		return transferHelper.getRecipeCategoryUid();
	}

	@Override
	public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean doTransfer) {
		IRecipeTransferHandlerHelper handlerHelper = Internal.getHelpers().recipeTransferHandlerHelper();
		StackHelper stackHelper = Internal.getStackHelper();

		if (!Config.isJeiOnServer()) {
			return handlerHelper.createInternalError();
		}
		
		Map<Integer, Slot> inventorySlots = new HashMap<>();
		for (Slot slot : transferHelper.getInventorySlots(container)) {
			inventorySlots.put(slot.slotNumber, slot);
		}

		Map<Integer, Slot> craftingSlots = new HashMap<>();
		for (Slot slot : transferHelper.getRecipeSlots(container)) {
			craftingSlots.put(slot.slotNumber, slot);
		}

		IRecipeWrapper recipeWrapper = recipeLayout.getRecipeWrapper();
		if (recipeWrapper.getInputs().size() > craftingSlots.size()) {
			Log.error("Recipe Transfer helper {} does not work for container {}", transferHelper.getClass(), container.getClass());
			return handlerHelper.createInternalError();
		}

		List<ItemStack> availableItemStacks = new ArrayList<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots.values()) {
			if (slot.getHasStack()) {
				if (!slot.canTakeStack(player)) {
					Log.error("Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}", transferHelper.getClass(), container.getClass(), slot.slotNumber);
					return handlerHelper.createInternalError();
				}
				filledCraftSlotCount++;
				availableItemStacks.add(slot.getStack().copy());
			}
		}

		for (Slot slot : inventorySlots.values()) {
			if (slot.getHasStack()) {
				availableItemStacks.add(slot.getStack().copy());
			} else {
				emptySlotCount++;
			}
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		if (filledCraftSlotCount - recipeWrapper.getInputs().size() > emptySlotCount) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

		GuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		StackHelper.MatchingItemsResult matchingItemsResult = stackHelper.getMatchingItems(availableItemStacks, itemStackGroup.getGuiIngredients());

		if (matchingItemsResult.missingItems.size() > 0) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
			return handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
		}

		List<Integer> craftingSlotIndexes = new ArrayList<>(craftingSlots.keySet());
		Collections.sort(craftingSlotIndexes);

		List<Integer> inventorySlotIndexes = new ArrayList<>(inventorySlots.keySet());
		Collections.sort(inventorySlotIndexes);

		// check that the slots exist and can be altered
		for (Map.Entry<Integer, ItemStack> entry : matchingItemsResult.matchingItems.entrySet()) {
			int craftNumber = entry.getKey();
			int slotNumber = craftingSlotIndexes.get(craftNumber);
			if (slotNumber >= container.inventorySlots.size()) {
				Log.error("Recipes Transfer Helper {} references slot {} outside of the inventory's size {}", transferHelper.getClass(), slotNumber, container.inventorySlots.size());
				return handlerHelper.createInternalError();
			}
			Slot slot = container.getSlot(slotNumber);
			ItemStack stack = entry.getValue();
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
			PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		}

		return null;
	}

	public static void setItems(@Nonnull EntityPlayer player, @Nonnull Map<Integer, ItemStack> slotMap, @Nonnull List<Integer> craftingSlots, @Nonnull List<Integer> inventorySlots) {
		Container container = player.openContainer;
		StackHelper stackHelper = Internal.getStackHelper();

		// remove required recipe items
		List<ItemStack> removeRecipeItems = new ArrayList<>();
		for (ItemStack matchingStack : slotMap.values()) {
			ItemStack requiredStack = matchingStack.copy();
			while (requiredStack.stackSize > 0) {
				Slot inventorySlot = stackHelper.getSlotWithStack(container, craftingSlots, requiredStack);
				if (inventorySlot == null) {
					inventorySlot = stackHelper.getSlotWithStack(container, inventorySlots, requiredStack);
					if (inventorySlot == null) {
						Log.error("Couldn't find required items in inventory, even though they should be there.");
						// abort! put removed items back into the player's inventory somewhere so they're not lost
						List<Integer> allSlots = new ArrayList<>();
						allSlots.addAll(inventorySlots);
						allSlots.addAll(craftingSlots);
						for (ItemStack removedRecipeItem : removeRecipeItems) {
							stackHelper.addStack(container, allSlots, removedRecipeItem, true);
						}
						return;
					}
				}
				ItemStack removed = inventorySlot.decrStackSize(requiredStack.stackSize);
				removeRecipeItems.add(removed);
				requiredStack.stackSize -= removed.stackSize;
			}
		}

		// clear the crafting grid
		List<ItemStack> clearedCraftingItems = new ArrayList<>();
		for (Integer craftingSlotNumber : craftingSlots) {
			Slot craftingSlot = container.getSlot(craftingSlotNumber);
			if (craftingSlot != null && craftingSlot.getHasStack()) {
				ItemStack craftingItem = craftingSlot.decrStackSize(Integer.MAX_VALUE);
				clearedCraftingItems.add(craftingItem);
			}
		}

		// put items into the crafting grid
		for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
			ItemStack stack = entry.getValue();
			Integer craftNumber = entry.getKey();
			Integer slotNumber = craftingSlots.get(craftNumber);
			Slot slot = container.getSlot(slotNumber);
			slot.putStack(stack);
		}

		// put cleared items back into the player's inventory
		for (ItemStack oldCraftingItem : clearedCraftingItems) {
			stackHelper.addStack(container, inventorySlots, oldCraftingItem, true);
		}

		container.detectAndSendChanges();
	}
}
