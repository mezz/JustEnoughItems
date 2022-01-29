package mezz.jei.gui.ingredients;

import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientTooltipCallback;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;

import java.util.List;

@SuppressWarnings("deprecation")
public class LegacyTooltipAdapter<T> implements IGuiIngredientTooltipCallback {
	private final IIngredientType<T> ingredientType;
	private final ITooltipCallback<T> legacyTooltipCallback;

	public LegacyTooltipAdapter(IIngredientType<T> ingredientType, ITooltipCallback<T> legacyTooltipCallback) {
		this.ingredientType = ingredientType;
		this.legacyTooltipCallback = legacyTooltipCallback;
	}

	@Override
	public void onTooltip(IGuiIngredient<?> guiIngredient, List<Component> tooltip) {
		Object displayedIngredient = guiIngredient.getDisplayedIngredient();
		Class<? extends T> ingredientClass = this.ingredientType.getIngredientClass();
		if (ingredientClass.isInstance(displayedIngredient)) {
			T castIngredient = ingredientClass.cast(displayedIngredient);
			int slotIndex = guiIngredient.getSlotIndex();
			boolean isInput = guiIngredient.getRole() == RecipeIngredientRole.INPUT;
			legacyTooltipCallback.onTooltip(slotIndex, isInput, castIngredient, tooltip);
		}
	}
}
