package mezz.jei.plugins.vanilla.furnace;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.google.common.base.Preconditions;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;

public class FuelRecipe implements IRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final String smeltCountString;
	private final IDrawableAnimated flame;

	public FuelRecipe(IGuiHelper guiHelper, Collection<ItemStack> input, int burnTime) {
		Preconditions.checkArgument(burnTime > 0, "burn time must be greater than 0");
		List<ItemStack> inputList = new ArrayList<>(input);
		this.inputs = Collections.singletonList(inputList);

		this.smeltCountString = createSmeltCountString(burnTime);

		this.flame = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 114, 14, 14)
			.buildAnimated(burnTime, IDrawableAnimated.StartDirection.TOP, true);
	}

	public static String createSmeltCountString(int burnTime) {
		if (burnTime == 200) {
			return Translator.translateToLocal("gui.jei.category.fuel.smeltCount.single");
		}
		NumberFormat numberInstance = NumberFormat.getNumberInstance();
		numberInstance.setMaximumFractionDigits(2);
		String smeltCount = numberInstance.format(burnTime / 200f);
		return Translator.translateToLocalFormatted("gui.jei.category.fuel.smeltCount", smeltCount);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		flame.draw(minecraft, 1, 0);
		int stringWidth = minecraft.fontRenderer.getStringWidth(smeltCountString);
		int textX = 20 + Math.round((recipeWidth - 20 - stringWidth) / 2.0f);
		int textY = Math.round((recipeHeight - minecraft.fontRenderer.FONT_HEIGHT) / 2.0f);
		minecraft.fontRenderer.drawString(smeltCountString, textX, textY, Color.gray.getRGB());
	}
}
