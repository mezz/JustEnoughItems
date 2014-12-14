package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import mezz.jei.api.recipe.IRecipeWrapper;

public class FuelRecipe implements IRecipeWrapper {
	@Nonnull
	private final List<ItemStack> input;
	@Nullable
	private final String burnTimeString;

	public FuelRecipe(@Nonnull Collection<ItemStack> input, int burnTime) {
		this.input = new ArrayList<ItemStack>(input);
		this.burnTimeString = StatCollector.translateToLocalFormatted("gui.jei.furnaceBurnTime", burnTime);
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
	public void drawInfo(@Nonnull Minecraft minecraft) {
		FontRenderer fontRendererObj = minecraft.fontRenderer;
		fontRendererObj.drawString(burnTimeString, 20, 45, Color.gray.getRGB());
	}

}
