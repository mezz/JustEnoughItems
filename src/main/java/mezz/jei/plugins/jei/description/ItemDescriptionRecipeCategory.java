package mezz.jei.plugins.jei.description;

import javax.annotation.Nullable;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;
import net.minecraft.util.ResourceLocation;

public class ItemDescriptionRecipeCategory extends BlankRecipeCategory<ItemDescriptionRecipe> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final String localizedName;

	public ItemDescriptionRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		ResourceLocation recipeBackgroundResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);
		icon = guiHelper.createDrawable(recipeBackgroundResource, 196, 39, 16, 16);
		slotBackground = guiHelper.getSlotDrawable();
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

	@Nullable
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ItemDescriptionRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.setBackground(0, slotBackground);
		guiItemStacks.set(ingredients);
	}
}
