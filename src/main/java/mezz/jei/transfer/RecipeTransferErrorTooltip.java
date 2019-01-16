package mezz.jei.transfer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.Translator;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {
	private final List<String> message = new ArrayList<>();

	public RecipeTransferErrorTooltip(String message) {
		this.message.add(Translator.translateToLocal("jei.tooltip.transfer"));
		this.message.add(TextFormatting.RED + message);
	}

	@Override
	public Type getType() {
		return Type.USER_FACING;
	}

	@Override
	public void showError(Minecraft minecraft, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		TooltipRenderer.drawHoveringText(minecraft, message, mouseX, mouseY, 150);
	}
}
