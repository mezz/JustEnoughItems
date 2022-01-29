package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
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
	public void showError(PoseStack poseStack, int mouseX, int mouseY, int recipeX, int recipeY) {
		for (IRecipeSlotView slot : slots) {
			slot.drawHighlight(poseStack, HIGHLIGHT_COLOR, recipeX, recipeY);
		}

		super.showError(poseStack, mouseX, mouseY, recipeX, recipeY);
	}
}
