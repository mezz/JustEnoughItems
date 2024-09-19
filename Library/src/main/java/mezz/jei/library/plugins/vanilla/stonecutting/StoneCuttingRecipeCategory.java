package mezz.jei.library.plugins.vanilla.stonecutting;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Blocks;

public class StoneCuttingRecipeCategory implements IRecipeCategory<StonecutterRecipe> {
	public static final int width = 82;
	public static final int height = 34;

	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;
	private final IGuiHelper guiHelper;

	public StoneCuttingRecipeCategory(IGuiHelper guiHelper) {
		this.guiHelper = guiHelper;
		background = guiHelper.createBlankDrawable(width, height);
		icon = guiHelper.createDrawableItemLike(Blocks.STONECUTTER);
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
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().get(0));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 61,  9)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void draw(StonecutterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		IDrawableStatic recipeArrow = guiHelper.getRecipeArrow();
		recipeArrow.draw(guiGraphics, 26, 9);
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
