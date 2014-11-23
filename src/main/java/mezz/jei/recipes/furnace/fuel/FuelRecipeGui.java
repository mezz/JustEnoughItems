package mezz.jei.recipes.furnace.fuel;

import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipes.IRecipeWrapper;
import mezz.jei.recipes.furnace.FurnaceRecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.StatCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

public class FuelRecipeGui extends FurnaceRecipeGui {

	@Nonnull
	private String burnTimeString;

	@Override
	public void setGuiItemStacks(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper, @Nullable ItemStack focusStack) {
		FuelRecipe fuelRecipe = (FuelRecipe)recipeWrapper;

		guiItemStacks.setItemStack(fuelSlot, fuelRecipe.getInputs(), focusStack);

		int burnTime = TileEntityFurnace.getItemBurnTime(focusStack);
		this.burnTimeString = StatCollector.translateToLocalFormatted("gui.jei.furnaceBurnTime", burnTime);
	}

	@Override
	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		FontRenderer fontRendererObj = minecraft.fontRenderer;
		fontRendererObj.drawString(burnTimeString, 20, 45, Color.gray.getRGB());
	}
}
