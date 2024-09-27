package mezz.jei.library.plugins.vanilla.brewing;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrewingRecipeCategory extends AbstractRecipeCategory<IJeiBrewingRecipe> {
	private final IDrawable background;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated bubbles;
	private final IDrawableStatic blazeHeat;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.BREWING,
			Component.translatable("gui.jei.category.brewing"),
			guiHelper.createDrawableItemLike(Blocks.BREWING_STAND),
			114,
			61
		);
		Textures textures = Internal.getTextures();
		background = textures.getBrewingStandBackground();

		arrow = guiHelper.createAnimatedDrawable(textures.getBrewingStandArrow(),400, IDrawableAnimated.StartDirection.TOP, false);

		ITickTimer bubblesTickTimer = new BrewingBubblesTickTimer(guiHelper);
		bubbles = guiHelper.createAnimatedDrawable(textures.getBrewingStandBubbles(), bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

		blazeHeat = textures.getBrewingStandBlazeHeat();
	}

	@Override
	public void draw(IJeiBrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		background.draw(guiGraphics, 0, 1);
		blazeHeat.draw(guiGraphics, 5, 30);
		bubbles.draw(guiGraphics, 9, 1);
		arrow.draw(guiGraphics, 43, 3);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiBrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, IFocusGroup focuses) {
		int brewingSteps = recipe.getBrewingSteps();
		String brewingStepsString = brewingSteps < Integer.MAX_VALUE ? Integer.toString(brewingSteps) : "?";
		Component steps = Component.translatable("gui.jei.category.brewing.steps", brewingStepsString);

		builder.addText(steps, 70, 28, 42, 12)
			.setColor(0xFF808080);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IJeiBrewingRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> potionInputs = recipe.getPotionInputs();

		builder.addInputSlot(1, 37)
			.addItemStacks(potionInputs);

		builder.addInputSlot(24, 44)
			.addItemStacks(potionInputs);

		builder.addInputSlot(47, 37)
			.addItemStacks(potionInputs);

		builder.addInputSlot(24, 3)
			.addItemStacks(recipe.getIngredients());

		builder.addOutputSlot(81, 3)
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
