package mezz.jei.recipes.furnace;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IGuiHelper;
import mezz.jei.api.recipes.RecipeType;
import mezz.jei.gui.resource.DrawableRecipePng;
import mezz.jei.recipes.RecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.awt.Color;

public class FurnaceRecipeGui extends RecipeGui {

	private static final int inputSlot = 0;
	private static final int fuelSlot = 1;
	private static final int outputSlot = 2;

	private String experienceString;

	public FurnaceRecipeGui() {
		super(new DrawableRecipePng(RecipeType.FURNACE));

		IGuiHelper guiHelper = JEIManager.guiHelper;

		addItem(guiHelper.makeGuiItemStack(0, 0, 1));
		addItem(guiHelper.makeGuiItemStack(0, 36, 1));
		addItem(guiHelper.makeGuiItemStack(60, 18, 1));
	}

	@Override
	public void setRecipe(Object recipe, ItemStack focusStack) {
		super.setRecipe(recipe, focusStack);

		FurnaceRecipe furnaceRecipe = (FurnaceRecipe)recipe;

		float experience = furnaceRecipe.getExperience();
		if (experience > 0.0) {
			experienceString = StatCollector.translateToLocalFormatted("gui.jei.furnaceExperience", experience);
		} else {
			experienceString = null;
		}
	}

	@Override
	protected void setItemsFromRecipe(Object recipe, ItemStack focusStack) {
		FurnaceRecipe furnaceRecipe = (FurnaceRecipe)recipe;

		setItem(inputSlot, furnaceRecipe.getInput(), focusStack);
		setItem(outputSlot, furnaceRecipe.getOutput(), focusStack);
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
