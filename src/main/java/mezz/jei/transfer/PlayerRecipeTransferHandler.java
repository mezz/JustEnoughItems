package mezz.jei.transfer;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.ImmutableSet;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.config.ServerInfo;
import mezz.jei.gui.ingredients.GuiItemStackGroup;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketRecipeTransfer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerRecipeTransferHandler implements IRecipeTransferHandler<InventoryMenu, CraftingRecipe> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IRecipeTransferInfo<InventoryMenu, CraftingRecipe> transferHelper;

	public PlayerRecipeTransferHandler(IStackHelper stackhelper, IRecipeTransferHandlerHelper handlerHelper) {
		this.stackHelper = stackhelper;
		this.handlerHelper = handlerHelper;
		this.transferHelper = new BasicRecipeTransferInfo<>(InventoryMenu.class, CraftingRecipe.class, VanillaRecipeCategoryUid.CRAFTING, 1, 4, 9, 36);
	}

	@Override
	public Class<InventoryMenu> getContainerClass() {
		return transferHelper.getContainerClass();
	}

	@Override
	public Class<CraftingRecipe> getRecipeClass() {
		return transferHelper.getRecipeClass();
	}

	@Nullable
	@Override
	public IRecipeTransferError transferRecipe(InventoryMenu container, CraftingRecipe recipe, IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer) {
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

		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		int inputCount = 0;
		{
			// indexes that do not fit into the player crafting grid
			Set<Integer> badIndexes = ImmutableSet.of(2, 5, 6, 7, 8);

			int inputIndex = 0;
			for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values()) {
				if (ingredient.isInput()) {
					if (!ingredient.getAllIngredients().isEmpty()) {
						inputCount++;
						if (badIndexes.contains(inputIndex)) {
							Component tooltipMessage = new TranslatableComponent("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
							return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
						}
					}
					inputIndex++;
				}
			}
		}

		// compact the crafting grid into a 2x2 area
		List<IGuiIngredient<ItemStack>> guiIngredients = new ArrayList<>();
		for (IGuiIngredient<ItemStack> guiIngredient : itemStackGroup.getGuiIngredients().values()) {
			if (guiIngredient.isInput()) {
				guiIngredients.add(guiIngredient);
			}
		}
		IGuiItemStackGroup playerInvItemStackGroup = new GuiItemStackGroup(null, 0);
		int[] playerGridIndexes = {0, 1, 3, 4};
		for (int i = 0; i < 4; i++) {
			int index = playerGridIndexes[i];
			if (index < guiIngredients.size()) {
				IGuiIngredient<ItemStack> ingredient = guiIngredients.get(index);
				playerInvItemStackGroup.init(i, true, 0, 0);
				playerInvItemStackGroup.set(i, ingredient.getAllIngredients());
			}
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

		RecipeTransferUtil.MatchingItemsResult matchingItemsResult = RecipeTransferUtil.getMatchingItems(stackHelper, availableItemStacks, playerInvItemStackGroup.getGuiIngredients());

		if (matchingItemsResult.missingItems.size() > 0) {
			Component message = new TranslatableComponent("jei.tooltip.error.recipe.transfer.missing");
			matchingItemsResult = RecipeTransferUtil.getMatchingItems(stackHelper, availableItemStacks, itemStackGroup.getGuiIngredients());
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
			PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes, maxTransfer, false);
			Network.sendPacketToServer(packet);
		}

		return null;
	}
}
