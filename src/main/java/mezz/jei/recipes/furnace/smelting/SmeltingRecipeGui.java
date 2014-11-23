package mezz.jei.recipes.furnace.smelting;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.recipes.furnace.FurnaceRecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

public class SmeltingRecipeGui extends FurnaceRecipeGui {

	@Nullable
	private String experienceString;

	@Override
	public void setGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull Object recipe, @Nullable ItemStack focusStack) {
		SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;

		guiItemStacks.setItemStack(inputSlot, smeltingRecipe.getInput(), focusStack);
		guiItemStacks.setItemStack(outputSlot, smeltingRecipe.getOutput(), focusStack);

		float experience = smeltingRecipe.getExperience();
		if (experience > 0.0) {
			experienceString = StatCollector.translateToLocalFormatted("gui.jei.furnaceExperience", experience);
		} else {
			experienceString = null;
		}
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		if (experienceString != null) {
			FontRenderer fontRendererObj = minecraft.fontRenderer;
			fontRendererObj.drawString(experienceString, 69 - fontRendererObj.getStringWidth(experienceString) / 2, 0, Color.gray.getRGB());
		}
	}
}
