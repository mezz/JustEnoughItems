package mezz.jei.library.plugins.vanilla.anvil;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;

public class SmithingRecipeCategory implements IRecipeCategory<UpgradeRecipe> {
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slot;
	private final IDrawable recipeArrow;

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(125, 28);
		slot = guiHelper.getSlotDrawable();
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.SMITHING_TABLE));
		Textures textures = Internal.getTextures();
		recipeArrow = textures.getRecipeArrow();
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

		builder.addSlot(RecipeIngredientRole.INPUT, 1, 6)
			.addIngredients(recipeHelper.getBase(recipe))
			.setBackground(slot, -1, -1);

		builder.addSlot(RecipeIngredientRole.INPUT, 50, 6)
			.addIngredients(recipeHelper.getAddition(recipe))
			.setBackground(slot, -1, -1);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 6)
			.addItemStack(recipe.getResultItem())
			.setBackground(slot, -1, -1);
	}

	@Override
	public void draw(UpgradeRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
		recipeArrow.draw(stack, 79, 7);
	}

	@Override
	public boolean isHandled(UpgradeRecipe recipe) {
		return !recipe.isSpecial();
	}
}
