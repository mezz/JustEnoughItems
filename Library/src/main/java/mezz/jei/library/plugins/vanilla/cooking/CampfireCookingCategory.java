package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingCategory extends AbstractCookingCategory<CampfireCookingRecipe> {
	public CampfireCookingCategory(IGuiHelper guiHelper) {
		super(guiHelper, RecipeTypes.CAMPFIRE_COOKING, Blocks.CAMPFIRE, "gui.jei.category.campfire", 400, 82, 44);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CampfireCookingRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 1)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().get(0));

		builder.addOutputSlot(61, 9)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, CampfireCookingRecipe recipe, IRecipeSlotsView recipeSlotsView, IFocusGroup focuses) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		builder.addAnimatedRecipeArrow(cookTime, 26, 7);
		builder.addAnimatedRecipeFlame(300, 1, 20);

		addCookTime(builder, recipe);
	}
}
