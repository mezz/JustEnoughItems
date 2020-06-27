package mezz.jei.transfer;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collection;
import java.util.Map;

import net.minecraft.item.ItemStack;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;

public class RecipeTransferErrorSlots extends RecipeTransferErrorTooltip {
	private static final int HIGHLIGHT_COLOR = 0x66FF0000;

	private final Collection<Integer> slots;

	public RecipeTransferErrorSlots(String message, Collection<Integer> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(MatrixStack matrixStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = itemStackGroup.getGuiIngredients();
		for (Integer slotIndex : slots) {
			IGuiIngredient<ItemStack> ingredient = ingredients.get(slotIndex);
			ingredient.drawHighlight(matrixStack, HIGHLIGHT_COLOR, recipeX, recipeY);
		}

		super.showError(matrixStack, mouseX, mouseY, recipeLayout, recipeX, recipeY);
	}
}
