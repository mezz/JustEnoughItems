package mezz.jei.plugins.vanilla.furnace;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.crafting.FurnaceRecipe;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import mezz.jei.util.Translator;

public class FurnaceRecipeWrapper extends ShapelessRecipeWrapper<FurnaceRecipe> {
	public FurnaceRecipeWrapper(IJeiHelpers jeiHelpers, FurnaceRecipe furnaceRecipe) {
		super(jeiHelpers, furnaceRecipe);
	}

	@Override
	public void drawInfo(int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			String experienceString = Translator.translateToLocalFormatted("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.fontRenderer;
			int stringWidth = fontRenderer.getStringWidth(experienceString);
			fontRenderer.drawString(experienceString, recipeWidth - stringWidth, 0, Color.gray.getRGB());
		}
	}
}
