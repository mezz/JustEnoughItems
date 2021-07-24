package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

import mezz.jei.api.recipe.transfer.IRecipeTransferError.Type;

public class RecipeTransferErrorInternal implements IRecipeTransferError {
	public static final RecipeTransferErrorInternal INSTANCE = new RecipeTransferErrorInternal();

	private RecipeTransferErrorInternal() {

	}

	@Override
	public Type getType() {
		return Type.INTERNAL;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		// don't show anything
	}
}
