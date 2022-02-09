package mezz.jei.plugins.jei.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {
	private final IIngredientHelper<DebugIngredient> ingredientHelper;

	public DebugIngredientRenderer(IIngredientHelper<DebugIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(PoseStack poseStack, int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
		if (ingredient != null) {
			Minecraft minecraft = Minecraft.getInstance();
			Font font = getFontRenderer(minecraft, ingredient);
			font.draw(poseStack, "JEI", xPosition, yPosition, 0xFFFF0000);
			font.draw(poseStack, "#" + ingredient.getNumber(), xPosition, yPosition + 8, 0xFFFF0000);
			RenderSystem.setShaderColor(1, 1, 1, 1);
		}
	}

	@Override
	public List<Component> getTooltip(DebugIngredient ingredient, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(new TextComponent(displayName));
		TextComponent debugIngredient = new TextComponent("debug ingredient");
		tooltip.add(debugIngredient.withStyle(ChatFormatting.GRAY));
		return tooltip;
	}
}
