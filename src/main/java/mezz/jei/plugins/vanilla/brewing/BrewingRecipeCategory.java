package mezz.jei.plugins.vanilla.brewing;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.config.Constants;
import mezz.jei.gui.elements.DrawableAnimated;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class BrewingRecipeCategory implements IRecipeCategory<BrewingRecipeWrapper> {

	private static final int brewPotionSlot1 = 0;
	private static final int brewPotionSlot2 = 1;
	private static final int brewPotionSlot3 = 2;
	private static final int brewIngredientSlot = 3;
	private static final int outputSlot = 4; // for display only

	private final IDrawable background;
	private final IDrawable slotDrawable;
	private final String localizedName;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.createDrawable(location, 0, 0, 64, 60, 1, 0, 0, 40);
		localizedName = Translator.translateToLocal("gui.jei.category.brewing");

		IDrawableStatic brewArrowDrawable = guiHelper.createDrawable(location, 64, 0, 9, 28);
		arrow = guiHelper.createAnimatedDrawable(brewArrowDrawable, 400, IDrawableAnimated.StartDirection.TOP, false);

		IDrawableStatic brewBubblesDrawable = guiHelper.createDrawable(location, 73, 0, 12, 29);
		ITickTimer bubblesTickTimer = new BrewingBubblesTickTimer(guiHelper);
		bubbles = new DrawableAnimated(brewBubblesDrawable, bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

		blazeHeat = guiHelper.createDrawable(location, 64, 29, 18, 4);

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
	public String getModName() {
		return Constants.minecraftModName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		blazeHeat.draw(minecraft, 5, 30);
		bubbles.draw(minecraft, 8, 0);
		arrow.draw(minecraft, 42, 2);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BrewingRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init(brewPotionSlot1, true, 0, 36);
		itemStacks.init(brewPotionSlot2, true, 23, 43);
		itemStacks.init(brewPotionSlot3, true, 46, 36);
		itemStacks.init(brewIngredientSlot, true, 23, 2);
		itemStacks.init(outputSlot, false, 80, 2);

		itemStacks.setBackground(outputSlot, slotDrawable);

		itemStacks.set(ingredients);
	}

	private static class BrewingBubblesTickTimer implements ITickTimer {
		/**
		 * Similar to {@link net.minecraft.client.gui.inventory.GuiBrewingStand#BUBBLELENGTHS}
		 */
		private static final int[] BUBBLE_LENGTHS = new int[]{29, 23, 18, 13, 9, 5, 0};
		private final ITickTimer internalTimer;

		public BrewingBubblesTickTimer(IGuiHelper guiHelper) {
			this.internalTimer = guiHelper.createTickTimer(14, BUBBLE_LENGTHS.length - 1, false);
		}

		@Override
		public int getValue() {
			int timerValue = this.internalTimer.getValue();
			return BUBBLE_LENGTHS[timerValue];
		}

		@Override
		public int getMaxValue() {
			return BUBBLE_LENGTHS[0];
		}
	}
}
