package mezz.jei.library.plugins.vanilla.cooking;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.Block;

import static mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
import static mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends FurnaceVariantCategory<T> {
	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;
	protected final IGuiHelper guiHelper;
	protected final int regularCookTime;

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime) {
		super(guiHelper);
		this.background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
		this.regularCookTime = regularCookTime;
		this.icon = guiHelper.createDrawableItemLike(icon);
		this.localizedName = Component.translatable(translationKey);
		this.guiHelper = guiHelper;
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
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		animatedFlame.draw(poseStack, 1, 20);

		drawExperience(recipe, poseStack, 0);
		drawCookTime(recipe, poseStack, 45);
	}

	protected void drawExperience(T recipe, PoseStack poseStack, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			fontRenderer.draw(poseStack, experienceString, getWidth() - stringWidth, y, 0xFF808080);
		}
	}

	protected void drawCookTime(T recipe, PoseStack poseStack, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(timeString);
			fontRenderer.draw(poseStack, timeString, getWidth() - stringWidth, y, 0xFF808080);
		}
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		builder.addSlot(INPUT, 1, 1)
			.addIngredients(recipe.getIngredients().get(0));

		builder.addSlot(OUTPUT, 61, 19)
			.addItemStack(recipe.getResultItem());
	}

	@Override
	public boolean isHandled(T recipe) {
		return !recipe.isSpecial();
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder acceptor, T recipe, IFocusGroup focuses) {
		acceptor.addWidget(createCookingArrowWidget(recipe, 24, 18));
	}

	@Override
	public ResourceLocation getRegistryName(T recipe) {
		return recipe.getId();
	}

	protected IRecipeWidget createCookingArrowWidget(T recipe, int x, int y) {
		return new CookingArrowRecipeWidget<>(guiHelper, recipe, regularCookTime, x, y);
	}

	private static class CookingArrowRecipeWidget<T extends AbstractCookingRecipe> implements IRecipeWidget {
		private final IDrawableAnimated arrow;
		private final Rect2i area;

		public CookingArrowRecipeWidget(IGuiHelper guiHelper, T recipe, int regularCookTime, int x, int y) {
			int cookTime = recipe.getCookingTime();
			if (cookTime <= 0) {
				cookTime = regularCookTime;
			}
			this.arrow = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
				.buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false);
			this.area = new Rect2i(x, y, arrow.getWidth(), arrow.getHeight());
		}

		@Override
		public Rect2i getArea() {
			return area;
		}

		@Override
		public void draw(PoseStack poseStack, double mouseX, double mouseY) {
			arrow.draw(poseStack);
		}
	}
}
