package mezz.jei.library.plugins.vanilla.compostable;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiCompostingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class CompostableRecipeCategory extends AbstractRecipeCategory<IJeiCompostingRecipe> {
	public CompostableRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.COMPOSTING,
			Component.translatable("gui.jei.category.compostable"),
			guiHelper.createDrawableItemLike(Blocks.COMPOSTER),
			120,
			18
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiCompostingRecipe recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 1)
			.setStandardSlotBackground()
			.addItemStacks(recipe.getInputs());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiCompostingRecipe recipe, IFocusGroup focuses) {
		float chance = recipe.getChance();
		int chancePercent = (int) Math.floor(chance * 100);
		Component text = Component.translatable("gui.jei.category.compostable.chance", chancePercent);
		builder.addText(text, getWidth() - 24, getHeight())
			.setPosition(24, 0)
			.setTextAlignment(HorizontalAlignment.CENTER)
			.setTextAlignment(VerticalAlignment.CENTER)
			.setColor(0xFF808080);
	}

	@Override
	public ResourceLocation getRegistryName(IJeiCompostingRecipe recipe) {
		return recipe.getUid();
	}
}
