package mezz.jei.plugins.jei.ingredients;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {
	private final IIngredientHelper<DebugIngredient> ingredientHelper;

	public DebugIngredientRenderer(IIngredientHelper<DebugIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(MatrixStack matrixStack, int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
		if (ingredient != null) {
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			font.drawString(matrixStack, "JEI", xPosition, yPosition, 0xFFFF0000);
			font.drawString(matrixStack, "#" + ingredient.getNumber(), xPosition, yPosition + 8, 0xFFFF0000);
			RenderSystem.color4f(1, 1, 1, 1);
		}
	}

	@Override
	public List<ITextComponent> getTooltip(DebugIngredient ingredient, ITooltipFlag tooltipFlag) {
		List<ITextComponent> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(new StringTextComponent(displayName));
		StringTextComponent debugIngredient = new StringTextComponent("debug ingredient");
		tooltip.add(debugIngredient.mergeStyle(TextFormatting.GRAY));
		return tooltip;
	}
}
