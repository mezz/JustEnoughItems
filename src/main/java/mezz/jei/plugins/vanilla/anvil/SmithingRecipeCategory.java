package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.Constants;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import net.minecraft.network.chat.Component;

public class SmithingRecipeCategory implements IRecipeCategory<UpgradeRecipe> {

	private final IDrawable background;
	private final IDrawable icon;

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 168, 125, 18);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.SMITHING_TABLE));
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.SMITHING;
	}

	@Override
	public Class<? extends UpgradeRecipe> getRecipeClass() {
		return UpgradeRecipe.class;
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

//	@Override
//	public void setIngredients(UpgradeRecipe recipe, IIngredients ingredients) {
//		ingredients.setInputIngredients(Arrays.asList(recipe.base, recipe.addition));
//		ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
//	}
//
//	@Override
//	public void setRecipe(IRecipeLayout recipeLayout, UpgradeRecipe recipe, IIngredients ingredients) {
//		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
//
//		guiItemStacks.init(0, true, 0, 0);
//		guiItemStacks.init(1, true, 49, 0);
//		guiItemStacks.init(2, false, 107, 0);
//
//		guiItemStacks.set(ingredients);
//	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, UpgradeRecipe recipe, List<? extends IFocus<?>> focuses) {
		builder.addSlot(0, RecipeIngredientRole.INPUT, 0, 0)
			.addIngredients(recipe.base);

		builder.addSlot(1, RecipeIngredientRole.INPUT, 49, 0)
			.addIngredients(recipe.addition);

		builder.addSlot(2, RecipeIngredientRole.OUTPUT, 107, 0)
			.addIngredient(recipe.getResultItem());
	}

	@Override
	public boolean isHandled(UpgradeRecipe recipe) {
		return !recipe.isSpecial();
	}
}
