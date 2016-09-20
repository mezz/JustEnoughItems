package mezz.jei.plugins.vanilla.furnace;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class SmeltingRecipe extends VanillaRecipeWrapper {
	private final List<List<ItemStack>> input;
	private final List<ItemStack> outputs;

	public SmeltingRecipe(List<ItemStack> input, ItemStack output) {
		this.input = Collections.singletonList(input);
		this.outputs = Collections.singletonList(output);
	}

	public List<List<ItemStack>> getInputs() {
		return input;
	}

	public List<ItemStack> getOutputs() {
		return outputs;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance();
		float experience = furnaceRecipes.getSmeltingExperience(outputs.get(0));
		if (experience > 0) {
			String experienceString = Translator.translateToLocalFormatted("gui.jei.category.smelting.experience", experience);
			FontRenderer fontRendererObj = minecraft.fontRendererObj;
			int stringWidth = fontRendererObj.getStringWidth(experienceString);
			fontRendererObj.drawString(experienceString, recipeWidth - stringWidth, 0, Color.gray.getRGB());
		}
	}
}
