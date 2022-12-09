package mezz.jei.library.plugins.vanilla.cooking;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.Block;

import static mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
import static mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends FurnaceVariantCategory<T> {
	private final IDrawable background;
	private final int regularCookTime;
	private final IDrawable icon;
	private final Component localizedName;
	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime) {
		super(guiHelper);
		this.background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
		this.regularCookTime = regularCookTime;
		this.icon = guiHelper.createDrawableItemStack(new ItemStack(icon));
		this.localizedName = Component.translatable(translationKey);
		this.cachedArrows = CacheBuilder.newBuilder()
			.maximumSize(25)
			.build(new CacheLoader<>() {
				@Override
				public IDrawableAnimated load(Integer cookTime) {
					return guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
						.buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false);
				}
			});
	}

	protected IDrawableAnimated getArrow(T recipe) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		return this.cachedArrows.getUnchecked(cookTime);
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

		IDrawableAnimated arrow = getArrow(recipe);
		arrow.draw(poseStack, 24, 18);

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
}
