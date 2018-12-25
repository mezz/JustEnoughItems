package mezz.jei.plugins.vanilla.furnace;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.util.Translator;

public class FurnaceSmeltingCategory extends FurnaceRecipeCategory<FurnaceRecipeWrapper> {
	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;

	public FurnaceSmeltingCategory(IGuiHelper guiHelper) {
		super(guiHelper);
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
		icon = guiHelper.createDrawableIngredient(new ItemStack(Blocks.FURNACE));
		localizedName = Translator.translateToLocal("gui.jei.category.smelting");
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
	public void drawExtras() {
		animatedFlame.draw(1, 20);
		arrow.draw(24, 18);
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public String getModName() {
		return Constants.MINECRAFT_NAME;
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.FURNACE;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, FurnaceRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(outputSlot, false, 60, 18);

		guiItemStacks.set(ingredients);
	}
}
