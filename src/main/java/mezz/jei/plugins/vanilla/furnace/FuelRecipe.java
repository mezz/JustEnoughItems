package mezz.jei.plugins.vanilla.furnace;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class FuelRecipe extends BlankRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final String smeltCountString;
	private final String burnTimeString;
	private final IDrawableAnimated flame;

	public FuelRecipe(IGuiHelper guiHelper, Collection<ItemStack> input, int burnTime) {
		List<ItemStack> inputList = new ArrayList<ItemStack>(input);
		this.inputs = Collections.singletonList(inputList);

		if (burnTime == 200) {
			this.smeltCountString = Translator.translateToLocal("gui.jei.category.fuel.smeltCount.single");
		} else {
			NumberFormat numberInstance = NumberFormat.getNumberInstance();
			numberInstance.setMaximumFractionDigits(2);
			String smeltCount = numberInstance.format(burnTime / 200f);
			this.smeltCountString = Translator.translateToLocalFormatted("gui.jei.category.fuel.smeltCount", smeltCount);
		}

		this.burnTimeString = Translator.translateToLocalFormatted("gui.jei.category.fuel.burnTime", burnTime);

		ResourceLocation furnaceBackgroundLocation = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		IDrawableStatic flameDrawable = guiHelper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
		this.flame = guiHelper.createAnimatedDrawable(flameDrawable, burnTime, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, inputs);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		flame.draw(minecraft, 2, 0);
		minecraft.fontRendererObj.drawString(smeltCountString, 24, 8, Color.gray.getRGB());
		minecraft.fontRendererObj.drawString(burnTimeString, 24, 18, Color.gray.getRGB());
	}
}
