package mezz.jei.plugins.jei.description;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.Translator;

public class ItemDescriptionRecipeCategory extends BlankRecipeCategory<ItemDescriptionRecipe> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	private final IDrawable background;
	private final String localizedName;

	public ItemDescriptionRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		localizedName = Translator.translateToLocal("gui.jei.category.itemDescription");
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ItemDescriptionRecipe recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, false, xPos, 0);
		guiItemStacks.setFromRecipe(0, recipeWrapper.getOutputs());
	}
}
