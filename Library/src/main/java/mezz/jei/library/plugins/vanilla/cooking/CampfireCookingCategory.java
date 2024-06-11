package mezz.jei.library.plugins.vanilla.cooking;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.Constants;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingCategory extends AbstractCookingCategory<CampfireCookingRecipe> {
	private final IDrawable background;

	public CampfireCookingCategory(IGuiHelper guiHelper) {
		super(guiHelper, Blocks.CAMPFIRE, "gui.jei.category.campfire", 400);
		background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 186, 82, 34)
			.addPadding(0, 10, 0, 0)
			.build();
	}

	@Override
	public RecipeType<RecipeHolder<CampfireCookingRecipe>> getRecipeType() {
		return RecipeTypes.CAMPFIRE_COOKING;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void draw(RecipeHolder<CampfireCookingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		animatedFlame.draw(guiGraphics, 1, 20);
		IDrawableAnimated arrow = getArrow(recipeHolder);
		arrow.draw(guiGraphics, 24, 8);
		drawCookTime(recipeHolder, guiGraphics, 35);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CampfireCookingRecipe> recipeHolder, IFocusGroup focuses) {
		CampfireCookingRecipe recipe = recipeHolder.value();
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
			.addIngredients(recipe.getIngredients().getFirst());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9)
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}
}
