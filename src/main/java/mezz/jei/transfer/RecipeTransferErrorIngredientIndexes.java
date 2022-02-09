package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.gui.ingredients.RecipeSlot;
import net.minecraft.network.chat.Component;

import java.util.Set;

public class RecipeTransferErrorIngredientIndexes extends RecipeTransferErrorTooltip {
	private static final int HIGHLIGHT_COLOR = 0x66FF0000;

	private final Set<Integer> ingredientIndexes;

	public RecipeTransferErrorIngredientIndexes(Component message, Set<Integer> ingredientIndexes) {
		super(message);
		this.ingredientIndexes = ingredientIndexes;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
		poseStack.pushPose();
		{
			poseStack.translate(recipeX, recipeY, 0);

			for (IRecipeSlotView slotView : recipeSlotsView.getSlotViews()) {
				// casting this IRecipeSlotView to RecipeSlot is a hack for legacy support
				if (slotView instanceof RecipeSlot recipeSlot) {
					int legacyIngredientIndex = recipeSlot.getLegacyIngredientIndex();
					if (ingredientIndexes.contains(legacyIngredientIndex)) {
						recipeSlot.drawHighlight(poseStack, HIGHLIGHT_COLOR);
					}
				}
			}
		}
		poseStack.popPose();
	}
}
