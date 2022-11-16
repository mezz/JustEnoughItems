package mezz.jei.common.gui.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class OutputSlotTooltipCallback implements IRecipeSlotTooltipCallback {
	private final ResourceLocation recipeName;
	private final IModIdHelper modIdHelper;
	private final IRegisteredIngredients registeredIngredients;

	public OutputSlotTooltipCallback(
		ResourceLocation recipeName,
		IModIdHelper modIdHelper,
		IRegisteredIngredients registeredIngredients
	) {
		this.recipeName = recipeName;
		this.modIdHelper = modIdHelper;
		this.registeredIngredients = registeredIngredients;
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

		if (modIdHelper.isDisplayingModNameEnabled()) {
			ResourceLocation ingredientName = getResourceLocation(displayedIngredient.get());

			String recipeModId = recipeName.getNamespace();
			String ingredientModId = ingredientName.getNamespace();
			if (!recipeModId.equals(ingredientModId)) {
				String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
				MutableComponent recipeBy = Component.translatable("jei.tooltip.recipe.by", modName);
				tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
			}
		}

		Minecraft minecraft = Minecraft.getInstance();
		boolean showAdvanced = minecraft.options.advancedItemTooltips || Screen.hasShiftDown();
		if (showAdvanced) {
			MutableComponent recipeId = Component.translatable("jei.tooltip.recipe.id", recipeName.toString());
			tooltip.add(recipeId.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	private <T> ResourceLocation getResourceLocation(ITypedIngredient<T> ingredient) {
		IIngredientHelper<T> ingredientHelper = registeredIngredients.getIngredientHelper(ingredient.getType());
		return ingredientHelper.getResourceLocation(ingredient.getIngredient());
	}
}
