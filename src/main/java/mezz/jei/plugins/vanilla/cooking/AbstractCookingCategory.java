package mezz.jei.plugins.vanilla.cooking;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.config.Constants;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends FurnaceVariantCategory<T> {
	private final IDrawable background;
	private final int regularCookTime;
	private final IDrawable icon;
	private final ITextComponent localizedName;
	private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, icon, translationKey, regularCookTime, 82);
	}

	protected AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime, int width) {
		super(guiHelper);
		this.background = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54)
			.addPadding(0, 0, 0, width - 82)
			.build();
		this.regularCookTime = regularCookTime;
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(icon));
		this.localizedName = new TranslationTextComponent(translationKey);
		this.cachedArrows = CacheBuilder.newBuilder()
			.maximumSize(25)
			.build(new CacheLoader<Integer, IDrawableAnimated>() {
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
	public void setIngredients(T recipe, IIngredients ingredients) {
		if (recipe instanceof JeiFurnaceRecipe) {
			JeiFurnaceRecipe jeiRecipe = (JeiFurnaceRecipe) recipe;
			List<List<ItemStack>> inputs = new ArrayList<>();
			inputs.add(Arrays.asList(recipe.getIngredients().get(0).getItems()));
			inputs.add(Arrays.asList(jeiRecipe.getFuel().getItems()));
			ingredients.setInputLists(VanillaTypes.ITEM, inputs);
			ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(recipe.getResultItem(), jeiRecipe.getFuelOutput()));
		} else {
			ingredients.setInputIngredients(recipe.getIngredients());
			ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
		}
	}

	@Override
	public void draw(T recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
		animatedFlame.draw(matrixStack, 1, 20);

		IDrawableAnimated arrow = getArrow(recipe);
		int arrowX = background.getWidth() == 82 ? 24 : 44;
		arrow.draw(matrixStack, arrowX, 18);

		boolean hasFuelOutput = recipe instanceof JeiFurnaceRecipe && !((JeiFurnaceRecipe) recipe).getFuelOutput().isEmpty();
		if (hasFuelOutput) {
			drawExperienceCentered(recipe, matrixStack, 0);
			drawCookTimeCentered(recipe, matrixStack, 45);
		} else {
			drawExperience(recipe, matrixStack, 0);
			drawCookTime(recipe, matrixStack, 45);
		}
	}

	private void drawExperienceCentered(T recipe, MatrixStack matrixStack, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			TranslationTextComponent text = new TranslationTextComponent("gui.jei.category.smelting.experience", experience);
			drawCenteredInMiddle(matrixStack, text, y);
		}
	}

	private void drawCookTimeCentered(T recipe, MatrixStack matrixStack, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			TranslationTextComponent text = new TranslationTextComponent("gui.jei.category.smelting.time.seconds", cookTime / 20);
			drawCenteredInMiddle(matrixStack, text, y);
		}
	}

	private void drawCenteredInMiddle(MatrixStack matrixStack, ITextComponent text, int y) {
		FontRenderer fontRenderer = Minecraft.getInstance().font;
		int middleStart = 20;
		int middleWidth = background.getWidth() - 42;
		int x = middleStart + (middleWidth - fontRenderer.width(text)) / 2;
		fontRenderer.draw(matrixStack, text, x, y, 0xFF808080);
	}

	protected void drawExperience(T recipe, MatrixStack matrixStack, int y) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			TranslationTextComponent experienceString = new TranslationTextComponent("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			fontRenderer.draw(matrixStack, experienceString, background.getWidth() - stringWidth, y, 0xFF808080);
		}
	}

	protected void drawCookTime(T recipe, MatrixStack matrixStack, int y) {
		int cookTime = recipe.getCookingTime();
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			TranslationTextComponent timeString = new TranslationTextComponent("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(timeString);
			fontRenderer.draw(matrixStack, timeString, background.getWidth() - stringWidth, y, 0xFF808080);
		}
	}

	@Override
	@Deprecated
	public String getTitle() {
		return getTitleAsTextComponent().getString();
	}

	@Override
	public ITextComponent getTitleAsTextComponent() {
		return localizedName;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, T recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.init(inputSlot, true, 0, 0);
		if (background.getWidth() == 82) {
			guiItemStacks.init(outputSlot, false, 60, 18);
		} else {
			boolean hasFuelOutput = recipe instanceof JeiFurnaceRecipe && !((JeiFurnaceRecipe) recipe).getFuelOutput().isEmpty();
			guiItemStacks.init(1, true, 0, 36);
			guiItemStacks.init(2, false, 94, hasFuelOutput ? 4 : 18);
			guiItemStacks.init(3, false, 94, 32);
		}

		guiItemStacks.set(ingredients);
	}

	@Override
	public boolean isHandled(T recipe) {
		return !recipe.isSpecial();
	}
}
