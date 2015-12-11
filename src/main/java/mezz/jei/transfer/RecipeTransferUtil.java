package mezz.jei.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import mezz.jei.JustEnoughItems;
import mezz.jei.api.JEIManager;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.util.Log;
import mezz.jei.util.StackUtil;
import mezz.jei.util.Translator;

public class RecipeTransferUtil {
	public static IRecipeTransferError getTransferRecipeError(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		return transferRecipe(recipeLayout, player, false);
	}

	public static boolean transferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		IRecipeTransferError error = transferRecipe(recipeLayout, player, true);
		return error == null;
	}

	@Nullable
	private static IRecipeTransferError transferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean doTransfer) {
		Container container = player.openContainer;

		IRecipeTransferHelper transferHelper = JEIManager.recipeRegistry.getRecipeTransferHelper(container, recipeLayout.getRecipeCategory());
		if (transferHelper == null) {
			if (doTransfer) {
				Log.error("No Recipe Transfer helper for container {}", container.getClass());
			}
			return RecipeTransferErrorTransferHelper.instance;
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
			return RecipeTransferErrorTransferHelper.instance;
		}

		GuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		List<List<ItemStack>> requiredStackLists = getInputStacks(itemStackGroup.getGuiIngredients());

		List<ItemStack> availableItemStacks = new ArrayList<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots.values()) {
			if (slot.getHasStack()) {
				if (!slot.canTakeStack(player)) {
					Log.error("Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}", transferHelper.getClass(), container.getClass(), slot.slotNumber);
					return RecipeTransferErrorTransferHelper.instance;
				}
				filledCraftSlotCount++;
				availableItemStacks.add(slot.getStack());
			}
		}

		for (Slot slot : inventorySlots.values()) {
			if (slot.getHasStack()) {
				availableItemStacks.add(slot.getStack());
			} else {
				emptySlotCount++;
			}
		}

		// check if we have enough inventory space to shuffle items around to their final locations
		if (filledCraftSlotCount - recipeWrapper.getInputs().size() > emptySlotCount) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
			return new RecipeTransferErrorTooltip(message);
		}

		MatchingItemsResult matchingItemsResult = getMatchingItems(availableItemStacks, requiredStackLists);

		if (matchingItemsResult.missingItems.size() > 0) {
			Set<Integer> missingIngredientIndexes = new HashSet<>();
			for (Map.Entry<Integer, GuiIngredient<ItemStack>> guiIngredientEntry : itemStackGroup.getGuiIngredients().entrySet()) {
				GuiIngredient<ItemStack> guiIngredient = guiIngredientEntry.getValue();
				for (List<ItemStack> missingIngredients : matchingItemsResult.missingItems) {
					if (StackUtil.containsStack(guiIngredient.getAll(), missingIngredients) != null) {
						missingIngredientIndexes.add(guiIngredientEntry.getKey());
						break;
					}
				}
			}

			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
			return new RecipeTransferErrorSlots(message, missingIngredientIndexes);
		}

		Map<Integer, ItemStack> slotMap = buildSlotMap(itemStackGroup, matchingItemsResult.matchingItems);
		if (slotMap == null || StackUtil.containsSets(slotMap.values(), availableItemStacks) == null) {
			String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
			return new RecipeTransferErrorTooltip(message);
		}

		List<Integer> craftingSlotIndexes = new ArrayList<>(craftingSlots.keySet());
		Collections.sort(craftingSlotIndexes);

		List<Integer> inventorySlotIndexes = new ArrayList<>(inventorySlots.keySet());
		Collections.sort(inventorySlotIndexes);

		// check that the slots exist and can be altered
		for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
			int craftNumber = entry.getKey();
			int slotNumber = craftingSlotIndexes.get(craftNumber);
			if (slotNumber >= container.inventorySlots.size()) {
				Log.error("Recipes Transfer Helper {} references slot {} outside of the inventory's size {}", transferHelper.getClass(), slotNumber, container.inventorySlots.size());
				return RecipeTransferErrorTransferHelper.instance;
			}
			Slot slot = container.getSlot(slotNumber);
			ItemStack stack = entry.getValue();
			if (slot == null) {
				Log.error("The slot number {} does not exist in the container.", slotNumber);
				return RecipeTransferErrorTransferHelper.instance;
			}
			if (!slot.isItemValid(stack)) {
				Log.error("The ItemStack {} is not valid for the slot number {}", stack, slotNumber);
				return RecipeTransferErrorTransferHelper.instance;
			}
		}

		if (doTransfer) {
			PacketRecipeTransfer packet = new PacketRecipeTransfer(slotMap, craftingSlotIndexes, inventorySlotIndexes);
			JustEnoughItems.common.sendPacketToServer(packet);
		}

		return null;
	}

	/**
	 * Build slot map (Crafting Slot Number -> ItemStack) for the recipe.
	 * Based on slot position info from itemStackGroup the ingredients from the player's inventory in matchingStacks.
	 */
	@Nullable
	private static Map<Integer, ItemStack> buildSlotMap(@Nonnull GuiItemStackGroup itemStackGroup, @Nonnull List<ItemStack> matchingStacks) {
		Map<Integer, ItemStack> slotMap = new HashMap<>();
		Map<Integer, GuiIngredient<ItemStack>> ingredientsMap = itemStackGroup.getGuiIngredients();

		int recipeSlotNumber = -1;
		SortedSet<Integer> keys = new TreeSet<>(ingredientsMap.keySet());
		for (Integer key : keys) {

			GuiIngredient<ItemStack> guiIngredient = ingredientsMap.get(key);
			if (!guiIngredient.isInput()) {
				continue;
			}
			recipeSlotNumber++;

			List<ItemStack> requiredStacks = guiIngredient.getAll();
			if (requiredStacks.isEmpty()) {
				continue;
			}

			ItemStack matchingStack = StackUtil.containsStack(matchingStacks, requiredStacks);
			if (matchingStack != null) {
				slotMap.put(recipeSlotNumber, matchingStack);
				matchingStacks.remove(matchingStack);
			} else {
				return null;
			}
		}

		return slotMap;
	}

	public static void setItems(@Nonnull EntityPlayer player, @Nonnull Map<Integer, ItemStack> slotMap, @Nonnull List<Integer> craftingSlots, @Nonnull List<Integer> inventorySlots) {
		Container container = player.openContainer;

		// remove required recipe items
		List<ItemStack> removeRecipeItems = new ArrayList<>();
		for (ItemStack matchingStack : slotMap.values()) {
			ItemStack requiredStack = matchingStack.copy();
			while (requiredStack.stackSize > 0) {
				Slot inventorySlot = getSlotWithStack(container, craftingSlots, requiredStack);
				if (inventorySlot == null) {
					inventorySlot = getSlotWithStack(container, inventorySlots, requiredStack);
					if (inventorySlot == null) {
						Log.error("Couldn't find required items in inventory, even though they should be there.");
						// abort! put removed items back into the player's inventory somewhere so they're not lost
						List<Integer> allSlots = new ArrayList<>();
						allSlots.addAll(inventorySlots);
						allSlots.addAll(craftingSlots);
						for (ItemStack removedRecipeItem : removeRecipeItems) {
							StackUtil.addStack(container, allSlots, removedRecipeItem, true);
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
			StackUtil.addStack(container, inventorySlots, oldCraftingItem, true);
		}

		container.detectAndSendChanges();
	}

	private static List<List<ItemStack>> getInputStacks(@Nonnull Map<Integer, GuiIngredient<ItemStack>> guiItemStacks) {
		List<List<ItemStack>> inputStacks = new ArrayList<>();

		for (Map.Entry<Integer, GuiIngredient<ItemStack>> entry : guiItemStacks.entrySet()) {
			if (entry.getValue().isInput()) {
				inputStacks.add(entry.getValue().getAll());
			}
		}
		return inputStacks;
	}

	public static class MatchingItemsResult {
		@Nonnull
		public final List<ItemStack> matchingItems = new ArrayList<>();
		@Nonnull
		public final List<List<ItemStack>> missingItems = new ArrayList<>();
	}

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns null if there are not enough items in availableItemStacks.
	 */
	@Nonnull
	private static MatchingItemsResult getMatchingItems(@Nonnull List<ItemStack> availableItemStacks, @Nonnull List<List<ItemStack>> requiredStacksList) {
		MatchingItemsResult matchingItemResult = new MatchingItemsResult();

		for (List<ItemStack> requiredStacks : requiredStacksList) {
			if (requiredStacks.isEmpty()) {
				continue;
			}

			ItemStack matching = null;
			for (ItemStack requiredStack : requiredStacks) {
				if (StackUtil.containsStack(availableItemStacks, requiredStack) != null) {
					matching = requiredStack.copy();
					break;
				}
			}
			if (matching == null) {
				matchingItemResult.missingItems.add(requiredStacks);
			} else {
				matchingItemResult.matchingItems.add(matching);
			}
		}

		return matchingItemResult;
	}

	@Nullable
	private static Slot getSlotWithStack(@Nonnull Container container, @Nonnull Iterable<Integer> slotNumbers, @Nonnull ItemStack stack) {
		for (Integer slotNumber : slotNumbers) {
			Slot slot = container.getSlot(slotNumber);
			if (slot != null) {
				ItemStack slotStack = slot.getStack();
				if (StackUtil.isIdentical(stack, slotStack)) {
					return slot;
				}
			}
		}
		return null;
	}
}
