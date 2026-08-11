package mezz.jei.plugins.vanilla.furnace;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;

public class SmeltingRecipe implements IRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final ItemStack output;
	private final ItemStack fuelOutput;

	public SmeltingRecipe(List<ItemStack> inputs, ItemStack output) {
		this.inputs = Collections.singletonList(inputs);
		this.output = output;
		this.fuelOutput = ItemStack.EMPTY;
	}

	public SmeltingRecipe(List<ItemStack> inputs, List<ItemStack> fuels, ItemStack output, ItemStack fuelOutput) {
		this.inputs = Arrays.asList(inputs, fuels);
		this.output = output;
		this.fuelOutput = fuelOutput;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		if (fuelOutput.isEmpty()) {
			ingredients.setOutput(VanillaTypes.ITEM, output);
		} else {
			ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(output, fuelOutput));
		}
	}

	public boolean hasFuelOutput() {
		return !fuelOutput.isEmpty();
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		FurnaceRecipes furnaceRecipes = FurnaceRecipes.instance();
		float experience;
		try {
			experience = furnaceRecipes.getSmeltingExperience(output);
		} catch (RuntimeException ignored) {
			experience = 0;
		}
		if (experience > 0) {
			String experienceString = Translator.translateToLocalFormatted("gui.jei.category.smelting.experience", experience);
			FontRenderer fontRenderer = minecraft.fontRenderer;
			int stringWidth = fontRenderer.getStringWidth(experienceString);
			int textX;
			if (!hasFuelOutput()) {
				textX = recipeWidth - stringWidth;
			} else {
				int middleStart = 20;
				int middleWidth = recipeWidth - 42;
				textX = middleStart + (middleWidth - stringWidth) / 2;
			}
			fontRenderer.drawString(experienceString, textX, 0, Color.gray.getRGB());
		}
	}
}
