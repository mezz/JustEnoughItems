package mezz.jei.plugins.vanilla.cooking;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;

public class CampfireCategory extends AbstractCookingCategory<CampfireCookingRecipe> {
	private final IDrawable background;

	public CampfireCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.CAMPFIRE, "gui.jei.category.campfire", 400);
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 186, 82, 34);
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.CAMPFIRE;
	}

	@Override
	public Class<? extends CampfireCookingRecipe> getRecipeClass() {
		return CampfireCookingRecipe.class;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(CampfireCookingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		animatedFlame.draw(matrixStack, 1, 20);
		arrow.draw(matrixStack, 24, 8);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CampfireCookingRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		guiItemStacks.init(outputSlot, false, 60, 8);

		guiItemStacks.set(ingredients);
	}
}