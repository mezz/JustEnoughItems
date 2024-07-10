package mezz.jei.library.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView, int recipeX, int recipeY) {

	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip) {
		tooltip.addAll(message);
	}

	@Override
	public List<Component> getTooltip() {
		return Collections.unmodifiableList(message);
	}
}
