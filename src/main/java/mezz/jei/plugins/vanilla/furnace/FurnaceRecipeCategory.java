package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryUid;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

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
	@Override
	public IRecipeCategoryUid getUid() {
		return VanillaRecipeCategoryUid.FURNACE;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void init(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull IGuiFluidStackGroup guiFluidTanks) {
		guiItemStacks.init(inputSlot, 0, 0);
		guiItemStacks.init(fuelSlot, 0, 36);
		guiItemStacks.init(outputSlot, 60, 18);
	}

	@Override
	public void setRecipe(@Nonnull IGuiItemStackGroup guiItemStacks, @Nonnull IGuiFluidStackGroup guiFluidTanks, @Nonnull IRecipeWrapper recipeWrapper) {
		if (recipeWrapper instanceof FuelRecipe) {
			FuelRecipe fuelRecipeWrapper = (FuelRecipe) recipeWrapper;
			guiItemStacks.set(fuelSlot, fuelRecipeWrapper.getInputs());
		} else if (recipeWrapper instanceof SmeltingRecipe) {
			SmeltingRecipe smeltingRecipeWrapper = (SmeltingRecipe) recipeWrapper;
			guiItemStacks.set(inputSlot, smeltingRecipeWrapper.getInputs());
			guiItemStacks.set(outputSlot, smeltingRecipeWrapper.getOutputs());
		}
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}
}
