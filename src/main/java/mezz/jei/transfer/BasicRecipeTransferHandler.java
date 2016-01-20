package mezz.jei.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.Config;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.gui.ingredients.IGuiIngredient;
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
		return transferRecipe(container, recipeLayout, player, false, doTransfer);
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
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

		int inputCount = 0;
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values()) {
			if (ingredient.isInput() && !ingredient.getAll().isEmpty()) {
				inputCount++;
			}
		}

		if (inputCount > craftingSlots.size()) {
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
		if (filledCraftSlotCount - inputCount > emptySlotCount) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
			return handlerHelper.createUserErrorWithTooltip(message);
		}

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
			PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes, maxTransfer);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		}

		return null;
	}

	public static void setItems(@Nonnull EntityPlayer player, @Nonnull Map<Integer, ItemStack> slotMap, @Nonnull List<Integer> craftingSlots, @Nonnull List<Integer> inventorySlots, boolean maxTransfer) {
		Container container = player.openContainer;
		StackHelper stackHelper = Internal.getStackHelper();

		// remove required recipe items
		int removedSets = removeSetsFromInventory(container, slotMap.values(), craftingSlots, inventorySlots, maxTransfer);
		if (removedSets == 0) {
			return;
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
			if (stack.isStackable()) {
				int maxSets = stack.getMaxStackSize() / stack.stackSize;
				stack.stackSize *= Math.min(maxSets, removedSets);
			}
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

	private static int removeSetsFromInventory(@Nonnull Container container, @Nonnull Collection<ItemStack> required, @Nonnull List<Integer> craftingSlots, @Nonnull List<Integer> inventorySlots, boolean maxTransfer) {
		if (maxTransfer) {
			List<ItemStack> requiredCopy = new ArrayList<>();
			requiredCopy.addAll(required);

			int removedSets = 0;
			while (!requiredCopy.isEmpty() && removeSetsFromInventory(container, requiredCopy, craftingSlots, inventorySlots)) {
				removedSets++;
				Iterator<ItemStack> iterator = requiredCopy.iterator();
				while (iterator.hasNext()) {
					ItemStack stack = iterator.next();
					if (!stack.isStackable() || (stack.stackSize * (removedSets + 1) > stack.getMaxStackSize())) {
						iterator.remove();
					}
				}
			}
			return removedSets;
		} else {
			boolean success = removeSetsFromInventory(container, required, craftingSlots, inventorySlots);
			return success ? 1 : 0;
		}
	}

	private static boolean removeSetsFromInventory(@Nonnull Container container, @Nonnull Iterable<ItemStack> required, @Nonnull List<Integer> craftingSlots, @Nonnull List<Integer> inventorySlots) {
		final Map<Slot, ItemStack> originalSlotContents = new HashMap<>();

		for (ItemStack matchingStack : required) {
			final ItemStack requiredStack = matchingStack.copy();
			while (requiredStack.stackSize > 0) {
				final Slot slot = getSlotWithStack(container, requiredStack, craftingSlots, inventorySlots);
				if (slot == null) {
					// abort! put removed items back where the came from
					for (Map.Entry<Slot, ItemStack> slotEntry : originalSlotContents.entrySet()) {
						ItemStack stack = slotEntry.getValue();
						slotEntry.getKey().putStack(stack);
					}
					return false;
				}

				if (!originalSlotContents.containsKey(slot)) {
					originalSlotContents.put(slot, slot.getStack().copy());
				}

				ItemStack removed = slot.decrStackSize(requiredStack.stackSize);
				requiredStack.stackSize -= removed.stackSize;
			}
		}

		return true;
	}

	private static Slot getSlotWithStack(@Nonnull Container container, @Nonnull ItemStack stack, @Nonnull List<Integer> craftingSlots, @Nonnull List<Integer> inventorySlots) {
		StackHelper stackHelper = Internal.getStackHelper();

		Slot slot = stackHelper.getSlotWithStack(container, craftingSlots, stack);
		if (slot == null) {
			slot = stackHelper.getSlotWithStack(container, inventorySlots, stack);
		}

		return slot;
	}
}
