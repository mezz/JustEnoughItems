package mezz.jei.plugins.vanilla.anvil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class AnvilRecipeWrapper implements IRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final List<List<ItemStack>> output;
	@Nullable
	private Map<Integer, ? extends IGuiIngredient<ItemStack>> currentIngredients = null;
	@Nullable
	private ItemStack lastLeftStack;
	@Nullable
	private ItemStack lastRightStack;
	private int lastCost;

	public AnvilRecipeWrapper(List<ItemStack> leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
		this.inputs = Lists.newArrayList();
		this.inputs.add(leftInput);
		this.inputs.add(rightInputs);

		this.output = Collections.singletonList(outputs);
	}

	@Override
	public void drawInfo(int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
		if (currentIngredients == null) {
			return;
		}

		ItemStack newLeftStack = currentIngredients.get(0).getDisplayedIngredient();
		ItemStack newRightStack = currentIngredients.get(1).getDisplayedIngredient();

		if (newLeftStack == null || newRightStack == null) {
			return;
		}

		if (lastLeftStack == null || lastRightStack == null
			|| !ItemStack.areItemStacksEqual(lastLeftStack, newLeftStack)
			|| !ItemStack.areItemStacksEqual(lastRightStack, newRightStack)) {
			lastLeftStack = newLeftStack;
			lastRightStack = newRightStack;
			lastCost = AnvilRecipeMaker.findLevelsCost(lastLeftStack, lastRightStack);
		}

		if (lastCost != 0) {
			String costText = lastCost < 0 ? "err" : Integer.toString(lastCost);
			String text = I18n.format("container.repair.cost", costText);

			Minecraft minecraft = Minecraft.getInstance();
			int mainColor = 0xFF80FF20;
			EntityPlayerSP player = minecraft.player;
			if (player != null &&
				(lastCost >= 40 || lastCost > player.experienceLevel) &&
				!player.abilities.isCreativeMode) {
				// Show red if the player doesn't have enough levels
				mainColor = 0xFFFF6060;
			}

			drawRepairCost(minecraft, text, mainColor, recipeWidth);
		}
	}

	private void drawRepairCost(Minecraft minecraft, String text, int mainColor, int recipeWidth) {
		int shadowColor = 0xFF000000 | (mainColor & 0xFCFCFC) >> 2;
		int width = minecraft.fontRenderer.getStringWidth(text);
		int x = recipeWidth - 2 - width;
		int y = 27;

		// TODO 1.13 is this still needed?
//		if (minecraft.fontRenderer.getUnicodeFlag()) {
//			Gui.drawRect(x - 2, y - 2, x + width + 2, y + 10, 0xFF000000);
//			Gui.drawRect(x - 1, y - 1, x + width + 1, y + 9, 0xFF3B3B3B);
//		} else {
		minecraft.fontRenderer.drawString(text, x + 1, y, shadowColor);
		minecraft.fontRenderer.drawString(text, x, y + 1, shadowColor);
		minecraft.fontRenderer.drawString(text, x + 1, y + 1, shadowColor);
//		}

		minecraft.fontRenderer.drawString(text, x, y, mainColor);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutputLists(VanillaTypes.ITEM, output);
	}

	public void setCurrentIngredients(Map<Integer, ? extends IGuiIngredient<ItemStack>> currentIngredients) {
		this.currentIngredients = currentIngredients;
	}
}
