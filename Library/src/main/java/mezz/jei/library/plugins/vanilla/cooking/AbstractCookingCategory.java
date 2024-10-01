package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.Block;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends AbstractRecipeCategory<T> {
	protected final int regularCookTime;

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, 82, 54);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, Block icon, String translationKey, int regularCookTime, int width, int height) {
		super(
			recipeType,
			Component.translatable(translationKey),
			guiHelper.createDrawableItemLike(icon),
			width,
			height
		);
		this.regularCookTime = regularCookTime;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		builder.addInputSlot(1, 1)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().get(0));

		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 37)
			.setStandardSlotBackground();

		builder.addOutputSlot(61, 19)
			.setOutputSlotBackground()
			.addItemStack(RecipeUtil.getResultItem(recipe));
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		builder.addAnimatedRecipeArrow(cookTime)
			.setPosition(26, 17);
		builder.addAnimatedRecipeFlame(300)
			.setPosition(1, 20);

		addExperience(builder, recipe);
		addCookTime(builder, recipe);
	}

	protected void addExperience(IRecipeExtrasBuilder builder, T recipe) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
			builder.addText(experienceString, getWidth() - 20, 10)
				.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
				.setTextAlignment(HorizontalAlignment.RIGHT)
				.setColor(0xFF808080);
		}
	}

	protected void addCookTime(IRecipeExtrasBuilder builder, T recipe) {
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		if (cookTime > 0) {
			int cookTimeSeconds = cookTime / 20;
			Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", cookTimeSeconds);
			builder.addText(timeString, getWidth() - 20, 10)
				.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)
				.setTextAlignment(HorizontalAlignment.RIGHT)
				.setTextAlignment(VerticalAlignment.BOTTOM)
				.setColor(0xFF808080);
		}
	}

	@Override
	public boolean isHandled(T recipe) {
		return !recipe.isSpecial();
	}

	@Override
	public ResourceLocation getRegistryName(T recipe) {
		return recipe.getId();
	}
}
