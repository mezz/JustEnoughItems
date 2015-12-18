package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import mezz.jei.util.Translator;

public class FuelRecipe extends VanillaRecipeWrapper {
	@Nonnull
	private final List<List<ItemStack>> inputs;
	@Nonnull
	private final String burnTimeString;
	@Nonnull
	private final IDrawableAnimated flame;

	public FuelRecipe(@Nonnull Collection<ItemStack> input, int burnTime) {
		List<ItemStack> inputList = new ArrayList<>(input);
		this.inputs = Collections.singletonList(inputList);
		this.burnTimeString = Translator.translateToLocalFormatted("gui.jei.category.fuel.burnTime", burnTime);
		this.flame = JEIManager.guiHelper.createAnimatedDrawable(flameDrawable, burnTime, IDrawableAnimated.StartDirection.TOP, true);
	}

	@Nonnull
	@Override
	public List<List<ItemStack>> getInputs() {
		return inputs;
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
		minecraft.fontRendererObj.drawString(burnTimeString, 24, 12, Color.gray.getRGB());
	}

	@Override
	public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
		flame.draw(minecraft, 2, 0);
	}
}
