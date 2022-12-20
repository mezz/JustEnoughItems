package mezz.jei.library.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

public class RecipeTransferErrorIngredientIndexes extends RecipeTransferErrorTooltip {
	private static final int HIGHLIGHT_COLOR = 0x66FF0000;

	private final Set<Integer> missingItemIndexes;

	public RecipeTransferErrorIngredientIndexes(Component message, Set<Integer> missingItemIndexes) {
		super(message);
		this.missingItemIndexes = missingItemIndexes;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
		super.showError(poseStack, mouseX, mouseY, recipeSlotsView, recipeX, recipeY);
		poseStack.pushPose();
		{
			poseStack.translate(recipeX, recipeY, 0);

			List<IRecipeSlotView> slotViews = recipeSlotsView.getSlotViews();
			for (int index : missingItemIndexes) {
				if (index >= 0 && index < slotViews.size()) {
					IRecipeSlotView slotView = slotViews.get(index);
					slotView.drawHighlight(poseStack, HIGHLIGHT_COLOR);
				}
			}
		}
		poseStack.popPose();
	}
}
