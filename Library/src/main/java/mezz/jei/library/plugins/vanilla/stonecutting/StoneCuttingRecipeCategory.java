package mezz.jei.library.plugins.vanilla.stonecutting;

import com.mojang.serialization.Codec;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.block.Blocks;

public class StoneCuttingRecipeCategory implements IRecipeCategory<RecipeHolder<StonecutterRecipe>> {
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
	public RecipeType<RecipeHolder<StonecutterRecipe>> getRecipeType() {
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<StonecutterRecipe> recipeHolder, IFocusGroup focuses) {
		StonecutterRecipe recipe = recipeHolder.value();

		builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().getFirst());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 61,  9)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void draw(RecipeHolder<StonecutterRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		IDrawableStatic recipeArrow = guiHelper.getRecipeArrow();
		recipeArrow.draw(guiGraphics, 26, 9);
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
