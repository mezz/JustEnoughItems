package mezz.jei.plugins.vanilla.brewing;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
import mezz.jei.util.Translator;

public class BrewingRecipeCategory implements IRecipeCategory<BrewingRecipeWrapper> {

	private static final int brewPotionSlot1 = 0;
	private static final int brewPotionSlot2 = 1;
	private static final int brewPotionSlot3 = 2;
	private static final int brewIngredientSlot = 3;
	private static final int outputSlot = 4; // for display only

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotDrawable;
	private final String localizedName;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.drawableBuilder(location, 0, 0, 64, 60)
			.addPadding(1, 0, 0, 50)
			.build();
		icon = guiHelper.createDrawableIngredient(new ItemStack(Items.BREWING_STAND));
		localizedName = Translator.translateToLocal("gui.jei.category.brewing");

		arrow = guiHelper.drawableBuilder(location, 64, 0, 9, 28)
			.buildAnimated(400, IDrawableAnimated.StartDirection.TOP, false);

		ITickTimer bubblesTickTimer = new BrewingBubblesTickTimer(guiHelper);
		bubbles = guiHelper.drawableBuilder(location, 73, 0, 12, 29)
			.buildAnimated(bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

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
		return Constants.MINECRAFT_NAME;
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
