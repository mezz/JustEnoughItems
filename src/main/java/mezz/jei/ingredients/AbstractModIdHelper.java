package mezz.jei.ingredients;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;

public abstract class AbstractModIdHelper implements IModIdHelper {
	@Override
	public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (!isDisplayingModNameEnabled()) {
			return tooltip;
		}
		String modId = ingredientHelper.getDisplayModId(ingredient);
		String modName = getFormattedModNameForModId(modId);
		List<String> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(modName);
		return tooltipCopy;
	}
}
