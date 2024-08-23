package mezz.jei.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.common.Constants;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import static mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
import static mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends FurnaceVariantCategory<RecipeHolder<T>> {
	private final IDrawable background;
	private final IDrawable icon;
	private final Component localizedName;
	protected final IGuiHelper guiHelper;
	protected final int regularCookTime;

	public AbstractCookingCategory(IGuiHelper guiHelper, Block icon, String translationKey, int regularCookTime) {
		super(guiHelper);
		this.background = guiHelper.createDrawable(Constants.RECIPE_GUI_VANILLA, 0, 114, 82, 54);
		this.regularCookTime = regularCookTime;
		this.icon = guiHelper.createDrawableItemStack(new ItemStack(icon));
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
	public void draw(RecipeHolder<T> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		animatedFlame.draw(guiGraphics, 1, 20);

		drawExperience(recipeHolder, guiGraphics, 0);
		drawCookTime(recipeHolder, guiGraphics, 45);
	}

	protected void drawExperience(RecipeHolder<T> recipeHolder, GuiGraphics guiGraphics, int y) {
		T recipe = recipeHolder.value();
		float experience = recipe.getExperience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			guiGraphics.drawString(fontRenderer, experienceString, getWidth() - stringWidth, y, 0xFF808080, false);
		}
	}

	protected void drawCookTime(RecipeHolder<T> recipeHolder, GuiGraphics guiGraphics, int y) {
		T recipe = recipeHolder.value();
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();

		builder.addSlot(INPUT, 1, 1)
			.addIngredients(recipe.getIngredients().getFirst());

		builder.addSlot(OUTPUT, 61, 19)
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder acceptor, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		acceptor.addWidget(createCookingArrowWidget(recipeHolder, new ScreenPosition(24, 18)));
	}

	@Override
	public boolean isHandled(RecipeHolder<T> recipeHolder) {
		T recipe = recipeHolder.value();
		return !recipe.isSpecial();
	}

	@Override
	public ResourceLocation getRegistryName(RecipeHolder<T> recipe) {
		return recipe.id();
	}

	@Override
	public Codec<RecipeHolder<T>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}

	protected IRecipeWidget createCookingArrowWidget(RecipeHolder<T> recipeHolder, ScreenPosition position) {
		return new CookingArrowRecipeWidget<>(guiHelper, recipeHolder, regularCookTime, position);
	}

	private static class CookingArrowRecipeWidget<T extends AbstractCookingRecipe> implements IRecipeWidget {
		private final IDrawableAnimated arrow;
		private final ScreenPosition position;

		public CookingArrowRecipeWidget(IGuiHelper guiHelper, RecipeHolder<T> recipeHolder, int regularCookTime, ScreenPosition position) {
			T recipe = recipeHolder.value();
			int cookTime = recipe.getCookingTime();
			if (cookTime <= 0) {
				cookTime = regularCookTime;
			}
			this.arrow = guiHelper.drawableBuilder(Constants.RECIPE_GUI_VANILLA, 82, 128, 24, 17)
				.buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false);
			this.position = position;
		}

		@Override
		public ScreenPosition getPosition() {
			return position;
		}

		@Override
		public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
			arrow.draw(guiGraphics);
		}
	}
}
