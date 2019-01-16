package mezz.jei.transfer;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;

public class RecipeTransferErrorSlots extends RecipeTransferErrorTooltip {
	private static final Color highlightColor = new Color(1.0f, 0.0f, 0.0f, 0.4f);
	private final Collection<Integer> slots;

	public RecipeTransferErrorSlots(String message, Collection<Integer> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = itemStackGroup.getGuiIngredients();
		for (Integer slotIndex : slots) {
			IGuiIngredient<ItemStack> ingredient = ingredients.get(slotIndex);
			ingredient.drawHighlight(minecraft, highlightColor, recipeX, recipeY);
		}

		super.showError(minecraft, mouseX, mouseY, recipeLayout, recipeX, recipeY);
	}
}
