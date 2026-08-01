package mezz.jei.library.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class RecipeTransferErrorMissingSlots extends RecipeTransferErrorTooltip {
	private final Collection<IRecipeSlotView> slots;

	public RecipeTransferErrorMissingSlots(Component message, Collection<IRecipeSlotView> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
		poseStack.pushPose();
		{
			poseStack.translate(recipeX, recipeY, 0);

			for (IRecipeSlotView slot : slots) {
				slot.drawHighlight(poseStack, JeiGuiColors.getColor(GuiColor.RECIPE_TRANSFER_MISSING_SLOT_HIGHLIGHT));
			}
		}
		poseStack.popPose();
	}

	@Override
	public int getMissingCountHint() {
		return this.slots.size();
	}
}
