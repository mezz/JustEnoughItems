package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipeWrapper> {

	private final IDrawable background;

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18, 0, 20, 0, 0);
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
	public String getModName() {
		return Constants.minecraftModName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AnvilRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 10-10, 0);
		guiItemStacks.init(1, true, 59-10, 0);
		guiItemStacks.init(2, false, 117-10, 0);

		guiItemStacks.set(ingredients);

		recipeWrapper.setCurrentIngredients(guiItemStacks.getGuiIngredients());
	}
}
