package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingCategory extends AbstractCookingCategory<CampfireCookingRecipe> {
	public CampfireCookingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.CAMPFIRE_COOKING, Blocks.CAMPFIRE, "gui.jei.category.campfire", 400, 82, 44);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CampfireCookingRecipe> recipeHolder, IFocusGroup focuses) {
		CampfireCookingRecipe recipe = recipeHolder.value();
		builder.addInputSlot(1, 1)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().getFirst());

		builder.addOutputSlot(61, 9)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<CampfireCookingRecipe> recipeHolder, IFocusGroup focuses) {
		CampfireCookingRecipe recipe = recipeHolder.value();
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		builder.addAnimatedRecipeArrow(cookTime)
			.setPosition(26, 7);
		builder.addAnimatedRecipeFlame(300)
			.setPosition(1, 20);

		addCookTime(builder, recipeHolder);
	}
}
