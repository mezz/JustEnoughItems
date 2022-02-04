package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

@Deprecated(forRemoval = true)
public class RecipeTransferErrorIngredientIndexes extends RecipeTransferErrorTooltip {
	private static final int HIGHLIGHT_COLOR = 0x66FF0000;

	private final Set<Integer> ingredientIndexes;

	public RecipeTransferErrorIngredientIndexes(Component message, Set<Integer> ingredientIndexes) {
		super(message);
		this.ingredientIndexes = ingredientIndexes;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = itemStackGroup.getGuiIngredients();
		for (Integer ingredientIndex : ingredientIndexes) {
			IGuiIngredient<ItemStack> ingredient = ingredients.get(ingredientIndex);
			ingredient.drawHighlight(poseStack, HIGHLIGHT_COLOR, recipeX, recipeY);
		}

		super.showError(poseStack, mouseX, mouseY, recipeLayout, recipeX, recipeY);
	}
}
