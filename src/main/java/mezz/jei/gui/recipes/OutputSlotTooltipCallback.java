package mezz.jei.gui.recipes;

import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientTooltipCallback;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class OutputSlotTooltipCallback implements IGuiIngredientTooltipCallback {
	private final ResourceLocation recipeName;
	private final IModIdHelper modIdHelper;
	private final IIngredientManager ingredientManager;

	public OutputSlotTooltipCallback(
		ResourceLocation recipeName,
		IModIdHelper modIdHelper,
		IIngredientManager ingredientManager
	) {
		this.recipeName = recipeName;
		this.modIdHelper = modIdHelper;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public void onTooltip(IGuiIngredient<?> guiIngredient, List<Component> tooltip) {
		Object displayedIngredient = guiIngredient.getDisplayedIngredient();
		if (displayedIngredient == null) {
			return;
		}
		if (guiIngredient.getRole() != RecipeIngredientRole.OUTPUT) {
			return;
		}

		if (modIdHelper.isDisplayingModNameEnabled()) {
			ResourceLocation ingredientName = getResourceLocation(displayedIngredient);

			String recipeModId = recipeName.getNamespace();
			String ingredientModId = ingredientName.getNamespace();
			if (!recipeModId.equals(ingredientModId)) {
				String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
				TranslatableComponent recipeBy = new TranslatableComponent("jei.tooltip.recipe.by", modName);
				tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
			}
		}

		Minecraft minecraft = Minecraft.getInstance();
		boolean showAdvanced = minecraft.options.advancedItemTooltips || Screen.hasShiftDown();
		if (showAdvanced) {
			TranslatableComponent recipeId = new TranslatableComponent("jei.tooltip.recipe.id", recipeName.toString());
			tooltip.add(recipeId.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	private <T> ResourceLocation getResourceLocation(T ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return ingredientHelper.getResourceLocation(ingredient);
	}
}
