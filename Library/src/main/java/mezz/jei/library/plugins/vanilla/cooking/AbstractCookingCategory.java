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
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.library.recipes.CookingExtensionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class AbstractCookingCategory<T extends AbstractCookingRecipe> extends AbstractRecipeCategory<RecipeHolder<T>> implements IExtendableCookingRecipeCategory {
	protected final int regularCookTime;
	private final CookingExtensionHelper extensionHelper = new CookingExtensionHelper();

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
	public <R> void addExtension(Class<? extends R> recipeClass, ICookingCategoryExtension<R> extension) {
		extensionHelper.addExtension(recipeClass, extension);
	}

	public SlotDisplay getFuel(T recipe) {
		SlotDisplay fuel = CookingCategoryExtension.getDefaultFuel(recipe);
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension != null) {
			return extension.getFuel(recipe, fuel);
		}
		return fuel;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> recipeHolder, IFocusGroup focuses) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension == null) {
			return;
		}
		FuelDisplay fuelDisplay = FuelDisplay.create(getFuel(recipe));
		int outputX = getWidth() - 21;
		int outputY = 9;
		if (hasFuelSlot()) {
			outputY = 19;
			if (fuelDisplay.output() != null) {
				outputY = 5;
			}
		}

		IRecipeSlotBuilder inputSlot = builder.addInputSlot(1, 1)
			.setStandardSlotBackground();

		if (hasFuelSlot() && fuelDisplay.input() != SlotDisplay.Empty.INSTANCE) {
			RecipeIngredientRole fuelRole = RecipeIngredientRole.RENDER_ONLY;
			if (fuelDisplay.isSpecific()) {
				fuelRole = RecipeIngredientRole.INPUT;
			}
			builder.addSlot(fuelRole, 1, 37)
				.setStandardSlotBackground()
				.add(fuelDisplay.input());
		}

		IRecipeSlotBuilder outputSlot = builder.addOutputSlot(outputX, outputY)
			.setOutputSlotBackground();

		if (hasFuelSlot() && fuelDisplay.output() != null) {
			builder.addOutputSlot(outputX, 33)
				.setOutputSlotBackground()
				.add(fuelDisplay.output());
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
		FurnaceRecipeDisplay display = CookingCategoryExtension.getDisplay(recipe);
		int cookTime = recipe.cookingTime();
		if (display != null) {
			cookTime = display.duration();
		}
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
			experience = recipe.experience();
			if (display != null) {
				experience = display.experience();
			}
		}
		experience = extension.getExperience(recipe, experience);
		boolean hasFuelOutput = hasFuelSlot() && FuelDisplay.create(getFuel(recipe)).output() != null;
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
	public List<Identifier> getSourceRecipeIds(RecipeHolder<T> recipeHolder) {
		T recipe = recipeHolder.value();
		var extension = extensionHelper.getRecipeExtension(recipe);
		if (extension != null) {
			List<Identifier> sourceRecipeIds = extension.getSourceRecipeIds(recipe);
			if (sourceRecipeIds != null) {
				return sourceRecipeIds;
			}
		}
		return getDefaultSourceRecipeIds(recipeHolder);
	}

	protected List<Identifier> getDefaultSourceRecipeIds(RecipeHolder<T> recipeHolder) {
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
	public final Codec<RecipeHolder<T>> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		return codecHelper.getRecipeHolderCodec();
	}

	private record FuelDisplay(SlotDisplay input, @Nullable SlotDisplay output) {
		public static FuelDisplay create(SlotDisplay fuel) {
			if (fuel instanceof SlotDisplay.WithRemainder(SlotDisplay input, SlotDisplay remainder)) {
				return new FuelDisplay(input, remainder);
			}
			return new FuelDisplay(fuel, null);
		}

		public boolean isSpecific() {
			return CookingCategoryExtension.isSpecificFuel(input);
		}
	}
}
