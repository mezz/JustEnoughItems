package mezz.jei.plugins.jei.ingredients;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.plugins.jei.JEIInternalPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {
	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
		if (ingredient != null) {
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			font.drawString("JEI", xPosition, yPosition, Color.RED.getRGB());
			font.drawString("#" + ingredient.getNumber(), xPosition, yPosition + 8, Color.RED.getRGB());
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, DebugIngredient ingredient) {
		IIngredientHelper<DebugIngredient> ingredientHelper = JEIInternalPlugin.ingredientRegistry.getIngredientHelper(ingredient);
		String displayName = ingredientHelper.getDisplayName(ingredient);
		List<String> tooltip = new ArrayList<String>();
		tooltip.add(displayName);
		tooltip.add(TextFormatting.GRAY + "debug ingredient");
		return tooltip;
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, DebugIngredient ingredient) {
		return minecraft.fontRendererObj;
	}
}
