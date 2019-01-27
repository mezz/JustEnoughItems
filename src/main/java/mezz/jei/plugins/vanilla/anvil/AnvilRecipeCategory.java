package mezz.jei.plugins.vanilla.anvil;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipeWrapper> {

	private final IDrawable background;
	private final IDrawable icon;

	public AnvilRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18)
			.addPadding(0, 20, 0, 0)
			.build();
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.ANVIL));
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
		return Constants.MINECRAFT_NAME;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, AnvilRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, true, 49, 0);
		guiItemStacks.init(2, false, 107, 0);

		guiItemStacks.set(ingredients);

		AnvilRecipeDisplayData displayData = AnvilRecipeDataCache.getDisplayData(recipeWrapper);
		displayData.setCurrentIngredients(guiItemStacks.getGuiIngredients());
	}
}
