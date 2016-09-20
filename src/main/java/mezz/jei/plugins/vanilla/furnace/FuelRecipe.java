package mezz.jei.plugins.vanilla.furnace;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class FuelRecipe extends VanillaRecipeWrapper {
	private final List<List<ItemStack>> inputs;
	private final String burnTimeString;
	private final IDrawableAnimated flame;

	public FuelRecipe(IGuiHelper guiHelper, Collection<ItemStack> input, int burnTime) {
		List<ItemStack> inputList = new ArrayList<ItemStack>(input);
		this.inputs = Collections.singletonList(inputList);
		this.burnTimeString = Translator.translateToLocalFormatted("gui.jei.category.fuel.burnTime", burnTime);

		ResourceLocation furnaceBackgroundLocation = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
		IDrawableStatic flameDrawable = guiHelper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
		this.flame = guiHelper.createAnimatedDrawable(flameDrawable, burnTime, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Override
	public List<List<ItemStack>> getInputs() {
		return inputs;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		minecraft.fontRendererObj.drawString(burnTimeString, 24, 12, Color.gray.getRGB());
	}

	@Override
	public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {
		flame.draw(minecraft, 2, 0);
	}
}
