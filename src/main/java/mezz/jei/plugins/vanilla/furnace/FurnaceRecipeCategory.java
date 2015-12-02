package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

public abstract class FurnaceRecipeCategory implements IRecipeCategory {

	protected static final int inputSlot = 0;
	protected static final int fuelSlot = 1;
	protected static final int outputSlot = 2;

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
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		if (recipeWrapper instanceof FuelRecipe) {
			guiItemStacks.setFromRecipe(fuelSlot, recipeWrapper.getInputs());
		} else if (recipeWrapper instanceof SmeltingRecipe) {
			guiItemStacks.setFromRecipe(inputSlot, recipeWrapper.getInputs());
			guiItemStacks.setFromRecipe(outputSlot, recipeWrapper.getOutputs());
		}
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}
}
