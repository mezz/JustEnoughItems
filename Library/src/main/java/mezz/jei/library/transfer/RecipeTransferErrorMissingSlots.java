package mezz.jei.library.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class RecipeTransferErrorMissingSlots extends RecipeTransferErrorTooltip {
	private static final int HIGHLIGHT_COLOR = 0x66FF0000;

	private final Collection<IRecipeSlotView> slots;

	public RecipeTransferErrorMissingSlots(Component message, Collection<IRecipeSlotView> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(GuiGraphics guiGraphics, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
		super.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeX, recipeY);
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(recipeX, recipeY, 0);

			for (IRecipeSlotView slot : slots) {
				slot.drawHighlight(guiGraphics, HIGHLIGHT_COLOR);
			}
		}
		poseStack.popPose();
	}
}
