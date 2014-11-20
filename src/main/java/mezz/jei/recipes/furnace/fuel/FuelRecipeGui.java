package mezz.jei.recipes.furnace.fuel;

import mezz.jei.recipes.furnace.FurnaceRecipeGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.StatCollector;

import java.awt.Color;

public class FuelRecipeGui extends FurnaceRecipeGui {

	private String burnTimeString;

	@Override
	public void setRecipe(Object recipe, ItemStack focusStack) {
		super.setRecipe(recipe, focusStack);

		int burnTime = TileEntityFurnace.getItemBurnTime(focusStack);
		this.burnTimeString = StatCollector.translateToLocalFormatted("gui.jei.furnaceBurnTime", burnTime);
	}

	@Override
	protected void setItemsFromRecipe(Object recipe, ItemStack focusStack) {
		FuelRecipe fuelRecipe = (FuelRecipe)recipe;
		setItem(fuelSlot, fuelRecipe.getInput(), focusStack);
	}

	@Override
	public void drawForeground(Minecraft minecraft, int mouseX, int mouseY) {
		FontRenderer fontRendererObj = minecraft.fontRenderer;
		fontRendererObj.drawString(burnTimeString, 20, 45, Color.gray.getRGB());
		super.drawForeground(minecraft, mouseX, mouseY);
	}
}
