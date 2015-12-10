package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import mezz.jei.plugins.vanilla.VanillaRecipeWrapper;
import mezz.jei.util.Translator;

public class FuelRecipe extends VanillaRecipeWrapper {
	@Nonnull
	private final List<ItemStack> input;
	@Nullable
	private final String burnTimeString;

	public FuelRecipe(@Nonnull Collection<ItemStack> input, int burnTime) {
		this.input = new ArrayList<>(input);
		this.burnTimeString = Translator.translateToLocalFormatted("gui.jei.furnaceBurnTime", burnTime);
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs() {
		return input;
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs() {
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
		minecraft.fontRendererObj.drawString(burnTimeString, 24, 12, Color.gray.getRGB());
	}
}
