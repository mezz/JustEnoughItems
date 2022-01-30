package mezz.jei.gui.ingredients;

import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

import java.util.List;

@SuppressWarnings("deprecation")
public class LegacyTooltipAdapter<T> implements IRecipeSlotTooltipCallback {
	private final IIngredientType<T> ingredientType;
	private final ITooltipCallback<T> legacyTooltipCallback;

	public LegacyTooltipAdapter(IIngredientType<T> ingredientType, ITooltipCallback<T> legacyTooltipCallback) {
		this.ingredientType = ingredientType;
		this.legacyTooltipCallback = legacyTooltipCallback;
	}

	@Override
	public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
		T displayedIngredient = recipeSlotView.getDisplayedIngredient(ingredientType);
		if (displayedIngredient != null) {
			int slotIndex = recipeSlotView.getSlotIndex();
			boolean isInput = recipeSlotView.getRole() == RecipeIngredientRole.INPUT;
			legacyTooltipCallback.onTooltip(slotIndex, isInput, displayedIngredient, tooltip);
		}
	}
}
