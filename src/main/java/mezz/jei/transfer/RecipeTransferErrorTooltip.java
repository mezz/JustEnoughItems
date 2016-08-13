package mezz.jei.transfer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {
	@Nonnull
	private final List<String> message = new ArrayList<>();

	public RecipeTransferErrorTooltip(@Nonnull String message) {
		this.message.add(Constants.RECIPE_TRANSFER_TOOLTIP);
		this.message.add(TextFormatting.RED + message);
	}

	@Nonnull
	@Override
	public Type getType() {
		return Type.USER_FACING;
	}

	@Override
	public void showError(@Nonnull Minecraft minecraft, int mouseX, int mouseY, @Nonnull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		TooltipRenderer.drawHoveringText(minecraft, message, mouseX, mouseY);
	}
}
