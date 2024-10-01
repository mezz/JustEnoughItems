package mezz.jei.library.plugins.vanilla.stonecutting;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Blocks;

public class StoneCuttingRecipeCategory extends AbstractRecipeCategory<StonecutterRecipe> {
	public static final int width = 82;
	public static final int height = 34;

	public StoneCuttingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.STONECUTTING,
			Component.translatable("gui.jei.category.stoneCutter"),
			guiHelper.createDrawableItemLike(Blocks.STONECUTTER),
			82,
			34
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, StonecutterRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 9)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().get(0));

		builder.addOutputSlot(61,  9)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, StonecutterRecipe recipe, IFocusGroup focuses) {
		builder.addRecipeArrow().setPosition(26, 9);
	}

	@Override
	public boolean isHandled(StonecutterRecipe recipe) {
		return !recipe.isSpecial();
	}

	@Override
	public ResourceLocation getRegistryName(StonecutterRecipe recipe) {
		return recipe.getId();
	}
}
