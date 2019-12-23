package mezz.jei.plugins.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
	public void render(int xPosition, int yPosition, @Nullable DebugIngredient ingredient) {
		if (ingredient != null) {
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			font.drawString("JEI", xPosition, yPosition, 0xFFFF0000);
			font.drawString("#" + ingredient.getNumber(), xPosition, yPosition + 8, 0xFFFF0000);
			RenderSystem.color4f(1, 1, 1, 1);
		}
	}

	@Override
	public List<String> getTooltip(DebugIngredient ingredient, ITooltipFlag tooltipFlag) {
		List<String> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(displayName);
		tooltip.add(TextFormatting.GRAY + "debug ingredient");
		return tooltip;
	}
}
