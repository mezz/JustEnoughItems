package mezz.jei.library.gui.recipes.layout.builder;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

@SuppressWarnings("removal")
public class LegacyTooltipCallbackAdapter implements IRecipeSlotRichTooltipCallback {
	private final mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback callback;

	public LegacyTooltipCallbackAdapter(mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
		callback.onRichTooltip(recipeSlotView, tooltip);
	}
}
