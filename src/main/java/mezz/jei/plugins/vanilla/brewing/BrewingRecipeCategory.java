package mezz.jei.plugins.vanilla.brewing;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.util.Translator;

public class BrewingRecipeCategory implements IRecipeCategory {

	private static final int brewPotionSlot1 = 0;
	private static final int brewPotionSlot2 = 1;
	private static final int brewPotionSlot3 = 2;
	private static final int brewIngredientSlot = 3;
	private static final int outputSlot = 4; // for display only

	private static final int outputSlotX = 80;
	private static final int outputSlotY = 1;

	@Nonnull
	private final IDrawable background;
	@Nonnull
	private final IDrawable slotDrawable;
	@Nonnull
	private final String localizedName;
	@Nonnull
	private final IDrawableAnimated arrow;
	@Nonnull
	private final IDrawableAnimated bubbles;

	public BrewingRecipeCategory(IGuiHelper guiHelper) {
		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/brewing_stand.png");
		background = guiHelper.createDrawable(location, 55, 15, 64, 56, 0, 0, 0, 40);
		localizedName = Translator.translateToLocal("gui.jei.category.brewing");

		IDrawableStatic brewArrowDrawable = guiHelper.createDrawable(location, 176, 0, 9, 28);
		arrow = guiHelper.createAnimatedDrawable(brewArrowDrawable, 400, IDrawableAnimated.StartDirection.TOP, false);

		IDrawableStatic brewBubblesDrawable = guiHelper.createDrawable(location, 185, 0, 12, 29);
		bubbles = guiHelper.createAnimatedDrawable(brewBubblesDrawable, 20, IDrawableAnimated.StartDirection.BOTTOM, false);

		slotDrawable = guiHelper.getSlotDrawable();
	}

	@Nonnull
	@Override
	public String getUid() {
		return VanillaRecipeCategoryUid.BREWING;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		slotDrawable.draw(minecraft, outputSlotX, outputSlotY);
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
		bubbles.draw(minecraft, 10, 0);
		arrow.draw(minecraft, 42, 1);
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init(brewPotionSlot1, true, 0, 30);
		itemStacks.init(brewPotionSlot2, true, 23, 37);
		itemStacks.init(brewPotionSlot3, true, 46, 30);
		itemStacks.init(brewIngredientSlot, true, 23, 1);
		itemStacks.init(outputSlot, false, outputSlotX, outputSlotY);

		if (recipeWrapper instanceof BrewingRecipeWrapper) {
			List inputs = recipeWrapper.getInputs();

			itemStacks.setFromRecipe(brewPotionSlot1, inputs.get(brewPotionSlot1));
			itemStacks.setFromRecipe(brewPotionSlot2, inputs.get(brewPotionSlot2));
			itemStacks.setFromRecipe(brewPotionSlot3, inputs.get(brewPotionSlot3));
			itemStacks.setFromRecipe(brewIngredientSlot, inputs.get(brewIngredientSlot));
			itemStacks.setFromRecipe(outputSlot, recipeWrapper.getOutputs());
		}
	}
}
