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
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.List;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends AbstractRecipeCategory<T> {
	protected final int regularCookTime;
	private final List<ItemStack> furnaceFuels;

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, List.of());
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, Block icon, String translationKey, int regularCookTime, List<ItemStack> furnaceFuels) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, furnaceFuels, 82, 54);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, Block icon, String translationKey, int regularCookTime, int width, int height) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, List.of(), width, height);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<T> recipeType, Block icon, String translationKey, int regularCookTime, List<ItemStack> furnaceFuels, int width, int height) {
		super(
			recipeType,
			Component.translatable(translationKey),
			guiHelper.createDrawableItemLike(icon),
			width,
			height
		);
		this.regularCookTime = regularCookTime;
		this.furnaceFuels = furnaceFuels;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		FuelDisplay fuelDisplay = FuelDisplay.create(recipe);
		int outputX = getWidth() - 21;
		int outputY = fuelDisplay.output().isEmpty() ? 19 : 5;

		builder.addInputSlot(1, 1)
			.setStandardSlotBackground()
			.addIngredients(recipe.getIngredients().get(0));

		RecipeIngredientRole fuelRole = fuelDisplay.isSpecific() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.RENDER_ONLY;
		var fuelSlot = builder.addSlot(fuelRole, 1, 37)
			.setStandardSlotBackground();
		if (fuelDisplay.isSpecific()) {
			fuelSlot.addIngredients(fuelDisplay.input());
		} else {
			fuelSlot.addItemStacks(this.furnaceFuels);
		}

		ItemStack result = recipe instanceof JeiSmeltingRecipe jeiRecipe ? jeiRecipe.getResult() : RecipeUtil.getResultItem(recipe);
		builder.addOutputSlot(outputX, outputY)
			.setOutputSlotBackground()
			.addItemStack(result);

		if (!fuelDisplay.output().isEmpty()) {
			builder.addOutputSlot(outputX, 33)
				.setOutputSlotBackground()
				.addItemStack(fuelDisplay.output());
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
		boolean hasFuelOutput = !FuelDisplay.create(recipe).output().isEmpty();
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		builder.addAnimatedRecipeArrowWidget(cookTime)
			.setPosition(getArrowX(), 17);
		builder.addAnimatedRecipeFlameWidget(300)
			.setPosition(1, 20);

		addExperience(builder, recipe, hasFuelOutput);
		addCookTime(builder, recipe);
	}

	private int getArrowX() {
		if (getWidth() == 82) {
			return 26;
		}
		return (getWidth() - 24) / 2;
	}

	protected void addExperience(IRecipeExtrasBuilder builder, T recipe) {
		addExperience(builder, recipe, false);
	}

	protected void addExperience(IRecipeExtrasBuilder builder, T recipe, boolean hasFuelOutput) {
		float experience = recipe.getExperience();
		if (experience > 0) {
			Component experienceString = Component.translatable("gui.jei.category.smelting.experience", experience);
			if (hasFuelOutput) {
				builder.addText(experienceString, getWidth() - 42, 10)
					.setPosition(20, 0)
					.setTextAlignment(HorizontalAlignment.CENTER)
					.setColor(JeiGuiColors.getColor(GuiColor.RECIPE_COOKING_EXPERIENCE_TEXT));
			} else {
				builder.addText(experienceString, getWidth() - 20, 10)
					.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
					.setTextAlignment(HorizontalAlignment.RIGHT)
					.setColor(JeiGuiColors.getColor(GuiColor.RECIPE_COOKING_EXPERIENCE_TEXT));
			}
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
			builder.addText(timeString, getWidth() - 42, 10)
				.setPosition(0, 0, getWidth(), getHeight(), HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
				.setTextAlignment(HorizontalAlignment.CENTER)
				.setTextAlignment(VerticalAlignment.BOTTOM)
				.setColor(JeiGuiColors.getColor(GuiColor.RECIPE_COOKING_TIME_TEXT));
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

	private record FuelDisplay(Ingredient input, ItemStack output) {
		public static FuelDisplay create(AbstractCookingRecipe recipe) {
			if (recipe instanceof JeiSmeltingRecipe jeiRecipe) {
				return new FuelDisplay(jeiRecipe.getFuel(), jeiRecipe.getFuelOutput());
			}
			return new FuelDisplay(Ingredient.EMPTY, ItemStack.EMPTY);
		}

		public boolean isSpecific() {
			return !input.isEmpty();
		}
	}
}
