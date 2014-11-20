package mezz.jei.recipes.furnace.smelting;

import mezz.jei.recipes.furnace.FurnaceRecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.awt.Color;

public class SmeltingRecipeGui extends FurnaceRecipeGui {

	private String experienceString;

	@Override
	public void setRecipe(Object recipe, ItemStack focusStack) {
		super.setRecipe(recipe, focusStack);

		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;

		float experience = smeltingRecipe.getExperience();
		if (experience > 0.0) {
			experienceString = StatCollector.translateToLocalFormatted("gui.jei.furnaceExperience", experience);
		} else {
			experienceString = null;
		}
	}

	@Override
	protected void setItemsFromRecipe(Object recipe, ItemStack focusStack) {
		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;

		setItem(inputSlot, smeltingRecipe.getInput(), focusStack);
		setItem(outputSlot, smeltingRecipe.getOutput(), focusStack);
	}

	@Override
	public void drawForeground(Minecraft minecraft, int mouseX, int mouseY) {
		if (experienceString != null) {
			FontRenderer fontRendererObj = minecraft.fontRenderer;
			fontRendererObj.drawString(experienceString, 69 - fontRendererObj.getStringWidth(experienceString) / 2, 0, Color.gray.getRGB());
		}
		super.drawForeground(minecraft, mouseX, mouseY);
	}
}
