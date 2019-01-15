package mezz.jei.plugins.vanilla.furnace;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;

public class FurnaceFuelCategory extends FurnaceRecipeCategory<FuelRecipe> {
	private final IDrawableStatic background;
	private final IDrawableStatic flameTransparentBackground;
	private final String localizedName;

	public FurnaceFuelCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 134, 18, 34)
			.addPadding(0, 0, 0, 88)
			.build();

		flameTransparentBackground = guiHelper.createDrawable(Constants.RECIPE_BACKGROUND, 215, 0, 14, 14);
		localizedName = Translator.translateToLocal("gui.jei.category.fuel");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.FUEL;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nullable
	@Override
	public IDrawable getIcon() {
		return flameTransparentBackground;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FuelRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(fuelSlot, true, 0, 16);
		guiItemStacks.set(ingredients);
	}
}
