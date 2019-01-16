package mezz.jei.plugins.jei.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {
	private final IIngredientHelper<DebugIngredient> ingredientHelper;

	public DebugIngredientRenderer(IIngredientHelper<DebugIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
		if (ingredient != null) {
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			font.drawString("JEI", xPosition, yPosition, Color.RED.getRGB());
			font.drawString("#" + ingredient.getNumber(), xPosition, yPosition + 8, Color.RED.getRGB());
			GlStateManager.color(1, 1, 1, 1);
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, DebugIngredient ingredient, ITooltipFlag tooltipFlag) {
		List<String> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(displayName);
		tooltip.add(TextFormatting.GRAY + "debug ingredient");
		return tooltip;
	}
}
