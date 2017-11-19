package mezz.jei.plugins.jei.info;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class IngredientInfoRecipeCategory implements IRecipeCategory<IngredientInfoRecipe> {
	public static final int recipeWidth = 160;
	public static final int recipeHeight = 125;
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotBackground;
	private final String localizedName;

	public IngredientInfoRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(recipeWidth, recipeHeight);
		ResourceLocation recipeBackgroundResource = Constants.RECIPE_BACKGROUND;
		icon = guiHelper.createDrawable(recipeBackgroundResource, 196, 39, 16, 16);
		slotBackground = guiHelper.getSlotDrawable();
		localizedName = Translator.translateToLocal("gui.jei.category.itemInformation");
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.INFORMATION;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public String getModName() {
		return Constants.NAME;
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
	public void setRecipe(IRecipeLayout recipeLayout, IngredientInfoRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		int xPos = (recipeWidth - 18) / 2;
		guiItemStacks.init(0, true, xPos, 0);
		guiItemStacks.setBackground(0, slotBackground);
		guiItemStacks.set(ingredients);

		IGuiFluidStackGroup guiFluidStackGroup = recipeLayout.getFluidStacks();
		guiFluidStackGroup.init(0, true, xPos + 1, 1);
		guiFluidStackGroup.set(ingredients);
	}
}
