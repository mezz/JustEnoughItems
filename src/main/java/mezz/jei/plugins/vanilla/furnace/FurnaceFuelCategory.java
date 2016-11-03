package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nullable;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;
import net.minecraft.util.ResourceLocation;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {
	private final IDrawable background;
	private final IDrawable flame;
	private final String localizedName;

	public FurnaceFuelCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		background = guiHelper.createDrawable(backgroundLocation, 55, 38, 18, 32, 0, 0, 0, 80);

		ResourceLocation recipeBackgroundResource = new ResourceLocation(Constants.RESOURCE_DOMAIN, Constants.TEXTURE_RECIPE_BACKGROUND_PATH);
		flame = guiHelper.createDrawable(recipeBackgroundResource, 215, 0, 14, 14);
		localizedName = Translator.translateToLocal("gui.jei.category.fuel");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nullable
	@Override
	public IDrawable getIcon() {
		return flame;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FuelRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(fuelSlot, true, 0, 14);
		guiItemStacks.set(ingredients);
	}
}
