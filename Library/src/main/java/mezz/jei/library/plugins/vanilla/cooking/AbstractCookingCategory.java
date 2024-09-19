package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.Block;

import static mezz.jei.api.recipe.RecipeIngredientRole.*;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> implements IRecipeCategory<T> {
	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;
	protected final IGuiHelper guiHelper;
	protected final int regularCookTime;
	protected final IDrawableAnimated animatedFlame;

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, icon, translationKey, regularCookTime, 82, 54);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime, int width, int height) {
		this.background = guiHelper.createBlankDrawable(width, height);
		this.regularCookTime = regularCookTime;
		this.icon = guiHelper.createDrawableItemLike(icon);
		this.localizedName = Component.translatable(translationKey);
		this.guiHelper = guiHelper;
		this.animatedFlame = guiHelper.createAnimatedRecipeFlame(300);
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
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		animatedFlame.draw(guiGraphics, 1, 20);

		drawExperience(recipe, guiGraphics, 0);
		drawCookTime(recipe, guiGraphics, 45);
	}

	protected void drawExperience(T recipe, GuiGraphics guiGraphics, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			guiGraphics.drawString(fontRenderer, experienceString, getWidth() - stringWidth, y, 0xFF808080, false);
		}
	}

	protected void drawCookTime(T recipe, GuiGraphics guiGraphics, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(timeString);
			guiGraphics.drawString(fontRenderer, timeString, getWidth() - stringWidth, y, 0xFF808080, false);
		}
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		builder.addSlot(INPUT, 1, 1)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().get(0));

		builder.addSlot(RENDER_ONLY, 1, 37)
			.setStandardSlotBackground();

		builder.addSlot(OUTPUT, 61, 19)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder acceptor, T recipe, IFocusGroup focuses) {
		acceptor.addWidget(createCookingArrowWidget(recipe, 26, 17));
	}

	@Override
	public boolean isHandled(T recipe) {
		return !recipe.isSpecial();
	}

	@Override
	public ResourceLocation getRegistryName(T recipe) {
		return recipe.getId();
	}

	protected IRecipeWidget createCookingArrowWidget(T recipe, int x, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		IDrawableAnimated recipeArrow = guiHelper.createAnimatedRecipeArrow(cookTime);
		return guiHelper.createWidgetFromDrawable(recipeArrow, x, y);
	}
}
