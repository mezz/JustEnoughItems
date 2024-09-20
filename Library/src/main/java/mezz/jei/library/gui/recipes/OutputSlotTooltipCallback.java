package mezz.jei.library.gui.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class OutputSlotTooltipCallback implements IRecipeSlotTooltipCallback {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ResourceLocation recipeName;
	private final boolean recipeFromSameModAsCategory;

	public OutputSlotTooltipCallback(ResourceLocation recipeName, RecipeType<?> recipeType) {
		this.recipeName = recipeName;
		this.recipeFromSameModAsCategory = recipeName.getNamespace().equals(recipeType.getUid().getNamespace());
	}

	@Override
	public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
		if (recipeSlotView.getRole() != RecipeIngredientRole.OUTPUT) {
			return;
		}
		Optional<ITypedIngredient<?>> displayedIngredient = recipeSlotView.getDisplayedIngredient();
		if (displayedIngredient.isEmpty()) {
			return;
		}

		addRecipeBy(tooltip, displayedIngredient.get());

		Minecraft minecraft = Minecraft.getInstance();
		boolean showAdvanced = minecraft.options.advancedItemTooltips || Screen.hasShiftDown();
		if (showAdvanced) {
			MutableComponent recipeId = Component.translatable("jei.tooltip.recipe.id", Component.literal(recipeName.toString()));
			tooltip.add(recipeId.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	private void addRecipeBy(List<Component> tooltip, ITypedIngredient<?> displayedIngredient) {
		if (recipeFromSameModAsCategory) {
			return;
		}
		IModIdHelper modIdHelper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
		if (!modIdHelper.isDisplayingModNameEnabled()) {
			return;
		}
		String ingredientModId = getDisplayModId(displayedIngredient);
		if (ingredientModId == null) {
			return;
		}
		String recipeModId = recipeName.getNamespace();
		if (recipeModId.equals(ingredientModId)) {
			return;
		}
		String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
		MutableComponent recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
		tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
	}

	private <T> @Nullable String getDisplayModId(ITypedIngredient<T> typedIngredient) {
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();

		IIngredientType<T> type = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(type);
		try {
			return ingredientHelper.getDisplayModId(ingredient);
		} catch (RuntimeException e) {
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, type, ingredientManager);
			LOGGER.error("Caught exception from ingredient without a resource location: {}", ingredientInfo, e);
			return null;
		}
	}
}
