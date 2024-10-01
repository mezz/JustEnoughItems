package mezz.jei.library.plugins.vanilla.stonecutting;

import com.mojang.serialization.Codec;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Blocks;

public class StoneCuttingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<StonecutterRecipe>> {
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<StonecutterRecipe> recipeHolder, IFocusGroup focuses) {
		StonecutterRecipe recipe = recipeHolder.value();

		builder.addInputSlot(1, 9)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().getFirst());

		builder.addOutputSlot(61,  9)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<StonecutterRecipe> recipe, IFocusGroup focuses) {
		builder.addRecipeArrow().setPosition(26, 9);
	}

	@Override
	public boolean isHandled(RecipeHolder<StonecutterRecipe> recipeHolder) {
		StonecutterRecipe recipe = recipeHolder.value();
		return !recipe.isSpecial();
	}

	@Override
	public ResourceLocation getRegistryName(RecipeHolder<StonecutterRecipe> recipe) {
		return recipe.id();
	}

	@Override
	public Codec<RecipeHolder<StonecutterRecipe>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}
}
