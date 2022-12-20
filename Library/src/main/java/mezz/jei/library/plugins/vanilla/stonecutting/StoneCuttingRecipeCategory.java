package mezz.jei.library.plugins.vanilla.stonecutting;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.common.Constants;
import net.minecraft.network.chat.Component;

public class StoneCuttingRecipeCategory implements IRecipeCategory<StonecutterRecipe> {
	public static final int width = 82;
	public static final int height = 34;

	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;

	public StoneCuttingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.createDrawable(location, 0, 220, width, height);
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.STONECUTTER));
		localizedName = Component.translatable("gui.jei.category.stoneCutter");
	}

	@Override
	public RecipeType<StonecutterRecipe> getRecipeType() {
		return RecipeTypes.STONECUTTING;
	}

	@Override
	public Component getTitle() {
		return localizedName;
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
	public void setRecipe(IRecipeLayoutBuilder builder, StonecutterRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
			.addIngredients(recipe.getIngredients().get(0));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 61,  9)
			.addItemStack(recipe.getResultItem());
	}

	@Override
	public boolean isHandled(StonecutterRecipe recipe) {
		return !recipe.isSpecial();
	}
}
