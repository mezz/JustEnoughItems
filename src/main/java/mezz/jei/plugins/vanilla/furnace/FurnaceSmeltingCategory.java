package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.JEIManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory {
	@Nonnull
	private final IDrawable background;

	public FurnaceSmeltingCategory() {
		ResourceLocation location = new ResourceLocation("minecraft:textures/gui/container/furnace.png");
		background = JEIManager.guiHelper.createDrawable(location, 55, 16, 82, 54);
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.SMELTING;
	}

	@Override
	public void init(@Nonnull IRecipeLayout recipeLayout) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(outputSlot, false, 60, 18);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.setFromRecipe(inputSlot, recipeWrapper.getInputs());
		guiItemStacks.setFromRecipe(outputSlot, recipeWrapper.getOutputs());
	}
}
