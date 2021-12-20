package mezz.jei.plugins.vanilla.brewing;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.config.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class BrewingRecipeCategory implements IRecipeCategory<JeiBrewingRecipe> {

	private static final int brewPotionSlot1 = 0;
	private static final int brewPotionSlot2 = 1;
	private static final int brewPotionSlot3 = 2;
	private static final int brewIngredientSlot = 3;
	private static final int outputSlot = 4; // for display only

	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slotDrawable;
	private final Component localizedName;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = Constants.RECIPE_GUI_VANILLA;
		background = guiHelper.drawableBuilder(location, 0, 0, 64, 60)
			.addPadding(1, 0, 0, 50)
			.build();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(Blocks.BREWING_STAND));
		localizedName = new TranslatableComponent("gui.jei.category.brewing");

		arrow = guiHelper.drawableBuilder(location, 64, 0, 9, 28)
			.buildAnimated(400, IDrawableAnimated.StartDirection.TOP, false);

		ITickTimer bubblesTickTimer = new BrewingBubblesTickTimer(guiHelper);
		bubbles = guiHelper.drawableBuilder(location, 73, 0, 12, 29)
			.buildAnimated(bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

		blazeHeat = guiHelper.createDrawable(location, 64, 29, 18, 4);

		slotDrawable = guiHelper.getSlotDrawable();
	}

	@Override
	public ResourceLocation getUid() {
		return VanillaRecipeCategoryUid.BREWING;
	}

	@Override
	public Class<? extends JeiBrewingRecipe> getRecipeClass() {
		return JeiBrewingRecipe.class;
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
	public void setIngredients(JeiBrewingRecipe recipe, IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, recipe.getInputs());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getPotionOutput());
	}

	@Override
	public void draw(JeiBrewingRecipe recipe, PoseStack poseStack, double mouseX, double mouseY) {
		blazeHeat.draw(poseStack, 5, 30);
		bubbles.draw(poseStack, 8, 0);
		arrow.draw(poseStack, 42, 2);

		int brewingSteps = recipe.getBrewingSteps();
		String brewingStepsString = brewingSteps < Integer.MAX_VALUE ? Integer.toString(brewingSteps) : "?";
		TranslatableComponent steps = new TranslatableComponent("gui.jei.category.brewing.steps", brewingStepsString);
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.font.draw(poseStack, steps, 70, 28, 0xFF808080);
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, JeiBrewingRecipe recipe, IIngredients ingredients) {
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
		 * Similar to {@link BrewingStandScreen#BUBBLELENGTHS}
		 */
		@SuppressWarnings("JavadocReference")
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
