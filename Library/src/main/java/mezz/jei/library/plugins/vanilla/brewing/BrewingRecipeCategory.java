package mezz.jei.library.plugins.vanilla.brewing;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrewingRecipeCategory implements IRecipeCategory<IJeiBrewingRecipe> {
	private final IDrawable backgroundArea;
	private final IDrawable backgroundImage;
	private final IDrawable icon;
	private final Component localizedName;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		backgroundArea = guiHelper.createBlankDrawable(114, 61);
		Textures textures = Internal.getTextures();
		backgroundImage = textures.getBrewingStandBackground();
		icon = guiHelper.createDrawableItemLike(Blocks.BREWING_STAND);
		localizedName = Component.translatable("gui.jei.category.brewing");

		arrow = guiHelper.createAnimatedDrawable(textures.getBrewingStandArrow(),400, IDrawableAnimated.StartDirection.TOP, false);

		ITickTimer bubblesTickTimer = new BrewingBubblesTickTimer(guiHelper);
		bubbles = guiHelper.createAnimatedDrawable(textures.getBrewingStandBubbles(), bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

		blazeHeat = textures.getBrewingStandBlazeHeat();
	}

	@Override
	public RecipeType<IJeiBrewingRecipe> getRecipeType() {
		return RecipeTypes.BREWING;
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return backgroundArea;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void draw(IJeiBrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		backgroundImage.draw(guiGraphics, 0, 1);
		blazeHeat.draw(guiGraphics, 5, 30);
		bubbles.draw(guiGraphics, 9, 1);
		arrow.draw(guiGraphics, 43, 3);

		int brewingSteps = recipe.getBrewingSteps();
		String brewingStepsString = brewingSteps < Integer.MAX_VALUE ? Integer.toString(brewingSteps) : "?";
		Component steps = Component.translatable("gui.jei.category.brewing.steps", brewingStepsString);
		Minecraft minecraft = Minecraft.getInstance();
		guiGraphics.drawString(minecraft.font, steps, 70, 28, 0xFF808080, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiBrewingRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> potionInputs = recipe.getPotionInputs();

		builder.addSlot(RecipeIngredientRole.INPUT, 1, 37)
			.addItemStacks(potionInputs);

		builder.addSlot(RecipeIngredientRole.INPUT, 24, 44)
			.addItemStacks(potionInputs);

		builder.addSlot(RecipeIngredientRole.INPUT, 47, 37)
			.addItemStacks(potionInputs);

		builder.addSlot(RecipeIngredientRole.INPUT, 24, 3)
			.addItemStacks(recipe.getIngredients());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 3)
			.addItemStack(recipe.getPotionOutput())
			.setStandardSlotBackground();
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(IJeiBrewingRecipe recipe) {
		return recipe.getUid();
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
