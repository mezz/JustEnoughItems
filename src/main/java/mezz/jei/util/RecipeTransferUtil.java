package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class RecipeTransferUtil {
	public static boolean hasTransferHelper(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		Container container = player.openContainer;

		IRecipeTransferHelper transferHelper = JEIManager.recipeRegistry.getRecipeTransferHelper(container, recipeLayout.getRecipeCategory());
		return transferHelper != null;
	}

	public static boolean canTransferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		return transferRecipe(recipeLayout, player, false);
	}

	public static boolean transferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player) {
		return transferRecipe(recipeLayout, player, true);
	}

	private static boolean transferRecipe(@Nonnull RecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean doTransfer) {
		Container container = player.openContainer;

		IRecipeTransferHelper transferHelper = JEIManager.recipeRegistry.getRecipeTransferHelper(container, recipeLayout.getRecipeCategory());
		if (transferHelper == null) {
			return false;
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
			return false;
		}

		GuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		List<List<ItemStack>> requiredStackLists = getInputStacks(itemStackGroup.getGuiIngredients());

		List<ItemStack> availableItemStacks = new ArrayList<>();
		int filledCraftSlotCount = 0;
		int emptySlotCount = 0;

		for (Slot slot : craftingSlots.values()) {
			if (slot.getHasStack()) {
				if (!slot.canTakeStack(player)) {
					return false;
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
			return false;
		}

		List<ItemStack> matchingStacks = getMatchingItems(availableItemStacks, requiredStackLists);
		if (matchingStacks == null) {
			return false;
		}

		Map<Integer, ItemStack> slotMap = buildSlotMap(itemStackGroup, matchingStacks);
		if (slotMap == null || StackUtil.containsSets(slotMap.values(), availableItemStacks) == null) {
			return false;
		}

		// check that the slots exist and can be altered
		for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
			int slotIndex = entry.getKey();
			if (slotIndex >= container.inventorySlots.size()) {
				return false;
			}
			Slot slot = container.getSlot(slotIndex);
			ItemStack stack = entry.getValue();
			if (slot == null || !slot.isItemValid(stack)) {
				return false;
			}
		}

		if (doTransfer) {
			PacketRecipeTransfer packet = new PacketRecipeTransfer(slotMap, craftingSlots.keySet(), inventorySlots.keySet());
			JustEnoughItems.common.sendPacketToServer(packet);
		}

		return true;
	}

	/**
	 * Build slot map (Crafting Slot Index -> ItemStack) for the recipe.
	 * Based on slot position info from itemStackGroup the ingredients from the player's inventory in matchingStacks.
	 */
	@Nullable
	private static Map<Integer, ItemStack> buildSlotMap(@Nonnull GuiItemStackGroup itemStackGroup, @Nonnull List<ItemStack> matchingStacks) {
		Map<Integer, ItemStack> slotMap = new HashMap<>();

		for (Map.Entry<Integer, GuiIngredient<ItemStack>> entry : itemStackGroup.getGuiIngredients().entrySet()) {
			GuiIngredient<ItemStack> guiIngredient = entry.getValue();
			if (!guiIngredient.isInput()) {
				continue;
			}

			List<ItemStack> requiredStacks = guiIngredient.getAll();
			if (requiredStacks.isEmpty()) {
				continue;
			}

			ItemStack matchingStack = StackUtil.containsStack(matchingStacks, requiredStacks);
			if (matchingStack != null) {
				slotMap.put(entry.getKey(), matchingStack);
				matchingStacks.remove(matchingStack);
			} else {
				return null;
			}
		}

		return slotMap;
	}

	public static void setItems(@Nonnull EntityPlayer player, @Nonnull Map<Integer, ItemStack> slotMap, @Nonnull Collection<Integer> craftingSlots, @Nonnull Collection<Integer> inventorySlots) {
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
		for (Integer craftingSlotIndex : craftingSlots) {
			Slot craftingSlot = container.getSlot(craftingSlotIndex);
			if (craftingSlot != null && craftingSlot.getHasStack()) {
				ItemStack craftingItem = craftingSlot.decrStackSize(Integer.MAX_VALUE);
				clearedCraftingItems.add(craftingItem);
			}
		}

		// put items into the crafting grid
		for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
			ItemStack stack = entry.getValue();
			Integer slotIndex = entry.getKey();
			Slot slot = container.getSlot(slotIndex);
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

	/**
	 * Returns a list of items in slots that complete the recipe defined by requiredStacksList.
	 * Returns null if there are not enough items in slots.
	 */
	@Nullable
	private static List<ItemStack> getMatchingItems(@Nonnull List<ItemStack> availableItemStacks, @Nonnull List<List<ItemStack>> requiredStacksList) {
		List<ItemStack> matchingItems = new ArrayList<>();

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
				return null;
			} else {
				matchingItems.add(matching);
			}
		}

		return matchingItems;
	}

	@Nullable
	private static Slot getSlotWithStack(@Nonnull Container container, @Nonnull Iterable<Integer> slotIndexes, @Nonnull ItemStack stack) {
		for (Integer slotIndex : slotIndexes) {
			Slot slot = container.getSlot(slotIndex);
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
