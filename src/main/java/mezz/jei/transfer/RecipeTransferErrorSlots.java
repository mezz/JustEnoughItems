package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.network.chat.Component;

import java.util.Set;

public class RecipeTransferErrorSlots extends RecipeTransferErrorTooltip {
	private static final int HIGHLIGHT_COLOR = 0x66FF0000;

	private final Set<Integer> slots;

	public RecipeTransferErrorSlots(Component message, Set<Integer> slots) {
		super(message);
		this.slots = slots;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		recipeLayout.getItemStacks().getGuiIngredients().values().stream()
			.filter(i -> slots.contains(i.getSlotIndex()))
			.forEach(i -> i.drawHighlight(poseStack, HIGHLIGHT_COLOR, recipeX, recipeY));

		super.showError(poseStack, mouseX, mouseY, recipeLayout, recipeX, recipeY);
	}
}
