package mezz.jei.library.transfer;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.gui.TooltipRenderer;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {
	private final List<Component> message = new ArrayList<>();

	public RecipeTransferErrorTooltip(Component message) {
		this.message.add(Component.translatable("jei.tooltip.transfer"));
		MutableComponent messageTextComponent = message.copy();
		this.message.add(messageTextComponent.withStyle(ChatFormatting.RED));
	}

	@Override
	public Type getType() {
		return Type.USER_FACING;
	}

	@Override
	public void showError(GuiGraphics guiGraphics, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {
		TooltipRenderer.drawHoveringText(guiGraphics, message, mouseX, mouseY);
	}
}
