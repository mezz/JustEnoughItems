package mezz.jei.library.plugins.vanilla.cooking;

import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.ICookingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.IExtendableCookingRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.library.recipes.CookingExtensionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends AbstractRecipeCategory<RecipeHolder<T>> implements IExtendableCookingRecipeCategory {
	protected final int regularCookTime;
	private final List<ItemStack> furnaceFuels;
	private final CookingExtensionHelper extensionHelper = new CookingExtensionHelper();

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<RecipeHolder<T>> recipeType, Block icon, String translationKey, int regularCookTime) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, List.of());
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<RecipeHolder<T>> recipeType, Block icon, String translationKey, int regularCookTime, List<ItemStack> furnaceFuels) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, furnaceFuels, 82, 54);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<RecipeHolder<T>> recipeType, Block icon, String translationKey, int regularCookTime, int width, int height) {
		this(guiHelper, recipeType, icon, translationKey, regularCookTime, List.of(), width, height);
	}

	public AbstractCookingCategory(IGuiHelper guiHelper, RecipeType<RecipeHolder<T>> recipeType, Block icon, String translationKey, int regularCookTime, List<ItemStack> furnaceFuels, int width, int height) {
		super(
			recipeType,
			Component.translatable(translationKey),
			guiHelper.createDrawableItemLike(icon),
			width,
			height
		);
		this.regularCookTime = regularCookTime;
		this.furnaceFuels = List.copyOf(furnaceFuels);
	}

	@Override
	public <R> void addExtension(Class<? extends R> recipeClass, ICookingCategoryExtension<R> extension) {
		extensionHelper.addExtension(recipeClass, extension);
	}

	public @Nullable Ingredient getFuel(T recipe) {
		Ingredient fuel = CookingCategoryExtension.getDefaultFuel(recipe);
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension != null) {
			return extension.getFuel(recipe, fuel);
		}
		return fuel;
	}

	private ItemStack getFuelOutput(T recipe) {
		if (getFuel(recipe) == null) {
			return ItemStack.EMPTY;
		}
		ItemStack output = CookingCategoryExtension.getDefaultFuelOutput(recipe);
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension != null) {
			return extension.getFuelOutput(recipe, output);
		}
		return output;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension == null) {
			return;
		}
		FuelDisplay fuelDisplay = new FuelDisplay(getFuel(recipe), getFuelOutput(recipe));
		int outputX = getWidth() - 21;
		int outputY = 9;
		if (hasFuelSlot()) {
			outputY = 19;
			if (!fuelDisplay.output().isEmpty()) {
				outputY = 5;
			}
		}

		IRecipeSlotBuilder inputSlot = builder.addInputSlot(1, 1)
			.setStandardSlotBackground();

		if (hasFuelSlot() && fuelDisplay.input() != null) {
			RecipeIngredientRole fuelRole = RecipeIngredientRole.RENDER_ONLY;
			if (fuelDisplay.isSpecific()) {
				fuelRole = RecipeIngredientRole.INPUT;
			}
			var fuelSlot = builder.addSlot(fuelRole, 1, 37).setStandardSlotBackground();
			if (fuelDisplay.isSpecific()) {
				fuelSlot.addIngredients(fuelDisplay.input());
			} else {
				fuelSlot.addItemStacks(furnaceFuels);
			}
		}

		IRecipeSlotBuilder outputSlot = builder.addOutputSlot(outputX, outputY)
			.setOutputSlotBackground();

		if (hasFuelSlot() && !fuelDisplay.output().isEmpty()) {
			builder.addOutputSlot(outputX, 33)
				.setOutputSlotBackground()
				.addItemStack(fuelDisplay.output());
		}
		extension.setRecipe(recipe, builder, inputSlot, outputSlot, focuses);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension == null) {
			return;
		}
		int cookTime = recipe.getCookingTime();
		if (cookTime <= 0) {
			cookTime = regularCookTime;
		}
		cookTime = extension.getCookingTime(recipe, cookTime);
		if (cookTime <= 0) {
			throw new IllegalArgumentException("Cooking time must be greater than zero: " + cookTime);
		}
		int arrowY = 7;
		if (hasFuelSlot()) {
			arrowY = 17;
		}
		builder.addAnimatedRecipeArrowWidget(cookTime)
			.setPosition(getArrowX(), arrowY);
		builder.addAnimatedRecipeFlameWidget(300)
			.setPosition(1, 20);

		float experience = 0;
		if (hasFuelSlot()) {
			experience = recipe.getExperience();
		}
		experience = extension.getExperience(recipe, experience);
		boolean hasFuelOutput = hasFuelSlot() && !getFuelOutput(recipe).isEmpty();
		addExperience(builder, experience, hasFuelOutput);
		addCookTime(builder, cookTime);
		extension.createRecipeExtras(recipe, builder, focuses);
	}

	@Override
	public boolean isHandled(RecipeHolder<T> recipeHolder) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		return extension != null && extension.isHandled(recipe);
	}

	@Override
	public void onDisplayedIngredientsUpdate(RecipeHolder<T> recipeHolder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension != null) {
			extension.onDisplayedIngredientsUpdate(recipe, recipeSlots, focuses);
		}
	}

	@Override
	public List<ResourceLocation> getSourceRecipeIds(RecipeHolder<T> recipeHolder) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension != null) {
			List<ResourceLocation> sourceRecipeIds = extension.getSourceRecipeIds(recipe);
			if (sourceRecipeIds != null) {
				return sourceRecipeIds;
			}
		}
		return getDefaultSourceRecipeIds(recipeHolder);
	}

	protected List<ResourceLocation> getDefaultSourceRecipeIds(RecipeHolder<T> recipeHolder) {
		return super.getSourceRecipeIds(recipeHolder);
	}

	protected boolean hasFuelSlot() {
		return true;
	}

	private int getArrowX() {
		if (getWidth() == 82) {
			return 26;
		}
		return (getWidth() - 24) / 2;
	}

	private void addExperience(IRecipeExtrasBuilder builder, float experience, boolean hasFuelOutput) {
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

	private void addCookTime(IRecipeExtrasBuilder builder, int cookTime) {
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
	public ResourceLocation getRegistryName(RecipeHolder<T> recipe) {
		return recipe.id();
	}

	@Override
	public final Codec<RecipeHolder<T>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}

	private record FuelDisplay(@Nullable Ingredient input, ItemStack output) {
		public boolean isSpecific() {
			return CookingCategoryExtension.isSpecificFuel(input);
		}
	}
}
