package mezz.jei.gui.ingredients;

import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"removal"})
public class LegacyTooltipAdapter<T> implements IRecipeSlotTooltipCallback {
	private final IIngredientType<T> ingredientType;
	private final ITooltipCallback<T> legacyTooltipCallback;

	public LegacyTooltipAdapter(IIngredientType<T> ingredientType, ITooltipCallback<T> legacyTooltipCallback) {
		this.ingredientType = ingredientType;
		this.legacyTooltipCallback = legacyTooltipCallback;
	}

	@Override
	public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
		Optional<T> displayedIngredient = recipeSlotView.getDisplayedIngredient(ingredientType);
		if (displayedIngredient.isPresent() && recipeSlotView instanceof RecipeSlot recipeSlot) {
			// casting this IRecipeSlotView to RecipeSlot is a hack for legacy support
			int ingredientIndex = recipeSlot.getLegacyIngredientIndex();
			boolean isInput = recipeSlotView.getRole() == RecipeIngredientRole.INPUT;
			legacyTooltipCallback.onTooltip(ingredientIndex, isInput, displayedIngredient.get(), tooltip);
		}
	}
}
