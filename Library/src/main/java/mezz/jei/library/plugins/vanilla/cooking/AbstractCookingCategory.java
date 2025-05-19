package mezz.jei.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.block.Block;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends AbstractRecipeCategory<RecipeHolder<T>> {
	protected final int regularCookTime;

	public AbstractCookingCategory(IGuiHelper guiHelper, IRecipeHolderType<T> recipeType, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, 82, 54);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, IRecipeHolderType<T> recipeType, Block icon, String translationKey, int regularCookTime, int width, int height) {
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			builder.addInputSlot(1, 1)
				.setStandardSlotBackground()
				.add(furnaceRecipeDisplay.ingredient());

			builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 37)
				.setStandardSlotBackground()
				.add(furnaceRecipeDisplay.fuel());

			builder.addOutputSlot(61, 19)
				.setOutputSlotBackground()
				.add(furnaceRecipeDisplay.result());
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		RecipeDisplay display = recipe.display().getFirst();
		if (display instanceof FurnaceRecipeDisplay furnaceRecipeDisplay) {
			int cookTime = furnaceRecipeDisplay.duration();
			if (cookTime <= 0) {
				cookTime = regularCookTime;
			}
			builder.addAnimatedRecipeArrow(cookTime)
				.setPosition(26, 17);
			builder.addAnimatedRecipeFlame(300)
				.setPosition(1, 20);

			addExperience(builder, furnaceRecipeDisplay);
			addCookTime(builder, furnaceRecipeDisplay);
		}
	}

	protected void addExperience(IRecipeExtrasBuilder builder, FurnaceRecipeDisplay recipe) {
		float experience = recipe.experience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
			builder.addText(experienceString, getWidth() - 20, 10)
				.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
				.setTextAlignment(HorizontalAlignment.RIGHT)
				.setColor(0xFF808080);
		}
	}

	protected void addCookTime(IRecipeExtrasBuilder builder, FurnaceRecipeDisplay recipe) {
		int cookTime = recipe.duration();
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
	public final Codec<RecipeHolder<T>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}
}
