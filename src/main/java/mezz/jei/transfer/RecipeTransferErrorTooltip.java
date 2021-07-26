package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.network.chat.TranslatableComponent;

import mezz.jei.api.recipe.transfer.IRecipeTransferError.Type;

public class RecipeTransferErrorTooltip implements IRecipeTransferError {
	private final List<Component> message = new ArrayList<>();

	public RecipeTransferErrorTooltip(Component message) {
		this.message.add(new TranslatableComponent("jei.tooltip.transfer"));
		MutableComponent messageTextComponent = message.copy();
		this.message.add(messageTextComponent.withStyle(ChatFormatting.RED));
	}

	@Override
	public Type getType() {
		return Type.USER_FACING;
	}

	@Override
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
		TooltipRenderer.drawHoveringText(message, mouseX, mouseY, Constants.MAX_TOOLTIP_WIDTH, poseStack);
	}
}
