package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStacks;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

public class FurnaceRecipeCategory implements IRecipeCategory {

	private static final int inputSlot = 0;
	private static final int fuelSlot = 1;
	private static final int outputSlot = 2;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final String localizedName;

	public FurnaceRecipeCategory() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
		background = JEIManager.guiHelper.createDrawable(location, 55, 16, 82, 54);
		localizedName = StatCollector.translateToLocal("gui.jei.furnaceRecipes");
	}

	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void init(@Nonnull IGuiItemStacks guiItemStacks) {
		guiItemStacks.initItemStack(inputSlot, 0, 0);
		guiItemStacks.initItemStack(fuelSlot, 0, 36);
		guiItemStacks.initItemStack(outputSlot, 60, 18);
	}

	@Override
	public void setRecipe(@Nonnull IGuiItemStacks guiItemStacks, @Nonnull IRecipeWrapper recipeWrapper) {
		if (recipeWrapper instanceof FuelRecipe) {
			FuelRecipe fuelRecipeWrapper = (FuelRecipe)recipeWrapper;
			guiItemStacks.setItemStack(fuelSlot, fuelRecipeWrapper.getInputs());
		} else if (recipeWrapper instanceof SmeltingRecipe) {
			SmeltingRecipe smeltingRecipeWrapper = (SmeltingRecipe)recipeWrapper;
			guiItemStacks.setItemStack(inputSlot, smeltingRecipeWrapper.getInputs());
			guiItemStacks.setItemStack(outputSlot, smeltingRecipeWrapper.getOutputs());
		}
	}

	@Nonnull
	@Override
	public String getCategoryTitle() {
		return localizedName;
	}
}
