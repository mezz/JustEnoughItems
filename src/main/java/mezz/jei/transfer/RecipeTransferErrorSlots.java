package mezz.jei.transfer;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import mezz.jei.gui.RecipeLayout;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.gui.ingredients.GuiItemStackGroup;

public class RecipeTransferErrorSlots extends RecipeTransferErrorTooltip {
	private static final Color highlightColor = new Color(1.0f, 0.0f, 0.0f, 0.4f);
	private final Collection<Integer> slots;

	public RecipeTransferErrorSlots(String message, Collection<Integer> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull RecipeLayout recipeLayout) {
		super.showError(minecraft, mouseX, mouseY, recipeLayout);

		GuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		Map<Integer, GuiIngredient<ItemStack>> ingredients = itemStackGroup.getGuiIngredients();
		for (Integer slotIndex : slots) {
			GuiIngredient<ItemStack> ingredient = ingredients.get(slotIndex);
			ingredient.drawHighlight(minecraft, highlightColor, recipeLayout.getPosX(), recipeLayout.getPosY());
		}
	}
}
