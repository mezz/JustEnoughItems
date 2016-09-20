package mezz.jei.transfer;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {
	private final List<String> message = new ArrayList<String>();

	public RecipeTransferErrorTooltip(String message) {
		this.message.add(Constants.RECIPE_TRANSFER_TOOLTIP);
		this.message.add(TextFormatting.RED + message);
	}

	@Override
	public Type getType() {
		return Type.USER_FACING;
	}

	@Override
	public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		TooltipRenderer.drawHoveringText(minecraft, message, mouseX, mouseY);
	}
}
