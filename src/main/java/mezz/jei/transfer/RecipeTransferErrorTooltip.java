package mezz.jei.transfer;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.RecipeLayout;
import mezz.jei.gui.TooltipRenderer;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {
	@Nonnull
	private final String message;

	public RecipeTransferErrorTooltip(@Nonnull String message) {
		this.message = message;
	}

	@Override
	public Type getType() {
		return Type.USER_FACING;
	}

	@Override
	public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull RecipeLayout recipeLayout) {
		TooltipRenderer.drawHoveringText(minecraft, message, mouseX, mouseY);
	}
}
