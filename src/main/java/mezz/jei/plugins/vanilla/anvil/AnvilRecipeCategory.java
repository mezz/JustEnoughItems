package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class AnvilRecipeCategory extends BlankRecipeCategory<AnvilRecipeWrapper> {

	private final IDrawable background;

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation backgroundLocation = new ResourceLocation("textures/gui/container/anvil.png");
		background = guiHelper.createDrawable(backgroundLocation, 16, 40, 145, 37);
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.ANVIL;
	}

	@Override
	public String getTitle() {
		return Blocks.ANVIL.getLocalizedName();
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AnvilRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 10, 6);
		guiItemStacks.init(1, true, 59, 6);
		guiItemStacks.init(2, false, 117, 6);

		guiItemStacks.set(ingredients);

		recipeWrapper.setCurrentIngredients(guiItemStacks.getGuiIngredients());
	}
}
