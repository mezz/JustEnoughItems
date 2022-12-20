package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.Constants;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;

import net.minecraft.network.chat.Component;

public class SmithingRecipeCategory implements IRecipeCategory<UpgradeRecipe> {
	private final IDrawable background;
	private final IDrawable icon;

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18);
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.SMITHING_TABLE));
	}

	@Override
	public RecipeType<UpgradeRecipe> getRecipeType() {
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
	public void setRecipe(IRecipeLayoutBuilder builder, UpgradeRecipe recipe, IFocusGroup focuses) {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();

		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
			.addIngredients(recipeHelper.getBase(recipe));

		builder.addSlot(RecipeIngredientRole.INPUT, 50, 1)
			.addIngredients(recipeHelper.getAddition(recipe));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1)
			.addItemStack(recipe.getResultItem());
	}

	@Override
	public boolean isHandled(UpgradeRecipe recipe) {
		return !recipe.isSpecial();
	}
}
