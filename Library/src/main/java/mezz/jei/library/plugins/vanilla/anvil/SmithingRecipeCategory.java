package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Constants;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

public class SmithingRecipeCategory implements IRecipeCategory<SmithingRecipe> {
	private final IDrawable background;
	private final IDrawable icon;

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 168, 108, 18);
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.SMITHING_TABLE));
	}

	@Override
	public RecipeType<SmithingRecipe> getRecipeType() {
		return RecipeTypes.SMITHING;
	}

	@Override
	public Component getTitle() {
		return Blocks.SMITHING_TABLE.getName();
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
	public void setRecipe(IRecipeLayoutBuilder builder, SmithingRecipe recipe, IFocusGroup focuses) {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();

		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
				.addIngredients(recipeHelper.getTemplate(recipe));

		builder.addSlot(RecipeIngredientRole.INPUT, 19, 1)
			.addIngredients(recipeHelper.getBase(recipe));

		builder.addSlot(RecipeIngredientRole.INPUT, 37, 1)
			.addIngredients(recipeHelper.getAddition(recipe));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 1)
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public boolean isHandled(SmithingRecipe recipe) {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		return recipeHelper.isHandled(recipe);
	}
}
