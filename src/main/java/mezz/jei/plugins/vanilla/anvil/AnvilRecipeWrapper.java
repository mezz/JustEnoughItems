package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnvilRecipeWrapper extends BlankRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final List<List<ItemStack>> output;
	private final int cost;

	public AnvilRecipeWrapper(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs, int levelsCost) {
		this.inputs = Arrays.asList(Collections.singletonList(leftInput), rightInputs);
		this.output = Collections.singletonList(outputs);
		this.cost = levelsCost;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		if (cost >= 0) {
			String text = I18n.format("container.repair.cost", cost);

			int mainColor = 0xFF80FF20;
			if ((cost >= 40 || cost > minecraft.player.experienceLevel)
					&& !minecraft.player.capabilities.isCreativeMode) {
				// Show red if the player doesn't have enough levels
				mainColor = 0xFFFF6060;
			}

			drawRepairCost(minecraft, text, mainColor, recipeWidth);
		}
	}

	private void drawRepairCost(Minecraft minecraft, String text, int mainColor, int recipeWidth) {
		int shadowColor = 0xFF000000 | (mainColor & 0xFCFCFC) >> 2;
		int width = minecraft.fontRendererObj.getStringWidth(text);
		int x = recipeWidth - 2 - width;
		int y = 27;

		if (minecraft.fontRendererObj.getUnicodeFlag()) {
			Gui.drawRect(x - 2, y - 2, x + width + 2, y + 10, 0xFF000000);
			Gui.drawRect(x - 1, y - 1, x + width + 1, y + 9, 0xFF3B3B3B);
		} else {
			minecraft.fontRendererObj.drawString(text, x + 1, y, shadowColor);
			minecraft.fontRendererObj.drawString(text, x, y + 1, shadowColor);
			minecraft.fontRendererObj.drawString(text, x + 1, y + 1, shadowColor);
		}

		minecraft.fontRendererObj.drawString(text, x, y, mainColor);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, inputs);
		ingredients.setOutputLists(ItemStack.class, output);
	}
}
