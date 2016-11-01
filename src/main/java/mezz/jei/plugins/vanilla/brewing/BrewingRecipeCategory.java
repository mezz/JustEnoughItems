package mezz.jei.plugins.vanilla.brewing;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class BrewingRecipeCategory extends BlankRecipeCategory<BrewingRecipeWrapper> {

	private static final int brewPotionSlot1 = 0;
	private static final int brewPotionSlot2 = 1;
	private static final int brewPotionSlot3 = 2;
	private static final int brewIngredientSlot = 3;
	private static final int outputSlot = 4; // for display only

	private static final int outputSlotX = 80;
	private static final int outputSlotY = 1;

	private final IDrawable background;
	private final IDrawable slotDrawable;
	private final String localizedName;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/brewing_stand.png");
		background = guiHelper.createDrawable(location, 55, 15, 64, 60, 0, 0, 0, 40);
		localizedName = Translator.translateToLocal("gui.jei.category.brewing");

		IDrawableStatic brewArrowDrawable = guiHelper.createDrawable(location, 176, 0, 9, 28);
		arrow = guiHelper.createAnimatedDrawable(brewArrowDrawable, 400, IDrawableAnimated.StartDirection.TOP, false);

		IDrawableStatic brewBubblesDrawable = guiHelper.createDrawable(location, 185, 1, 12, 28);
		bubbles = guiHelper.createAnimatedDrawable(brewBubblesDrawable, 20, IDrawableAnimated.StartDirection.BOTTOM, false);

		blazeHeat = guiHelper.createDrawable(location, 176, 29, 18, 4);

		slotDrawable = guiHelper.getSlotDrawable();
	}

	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.BREWING;
	}

	@Override
	public String getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		slotDrawable.draw(minecraft, outputSlotX, outputSlotY);
		blazeHeat.draw(minecraft, 5, 29);
		bubbles.draw(minecraft, 8, 0);
		arrow.draw(minecraft, 42, 1);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BrewingRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init(brewPotionSlot1, true, 0, 35);
		itemStacks.init(brewPotionSlot2, true, 23, 42);
		itemStacks.init(brewPotionSlot3, true, 46, 35);
		itemStacks.init(brewIngredientSlot, true, 23, 1);
		itemStacks.init(outputSlot, false, outputSlotX, outputSlotY);

		itemStacks.set(ingredients);
	}
}
