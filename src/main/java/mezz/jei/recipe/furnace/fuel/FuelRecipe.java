package mezz.jei.recipe.furnace.fuel;

import mezz.jei.api.recipe.wrapper.IFuelRecipeWrapper;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FuelRecipe implements IFuelRecipeWrapper {
	@Nonnull
	private final List<ItemStack> input;
	@Nullable
	private String burnTimeString;

	public FuelRecipe(@Nonnull Collection<ItemStack> input, int burnTime) {
		this.input = new ArrayList<ItemStack>(input);
		this.burnTimeString = StatCollector.translateToLocalFormatted("gui.jei.furnaceBurnTime", burnTime);
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs() {
		return input;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		FontRenderer fontRendererObj = minecraft.fontRenderer;
		fontRendererObj.drawString(burnTimeString, 20, 45, Color.gray.getRGB());
	}

}
