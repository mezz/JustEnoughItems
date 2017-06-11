package mezz.jei.startup;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;
import net.minecraft.util.text.TextFormatting;

public abstract class AbstractModIdHelper implements IModIdHelper {
	@Override
	public <T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String modId = ingredientHelper.getModId(ingredient);
		return getModNameForModId(modId);
	}

	@Override
	public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		String modNameFormat = Config.getModNameFormat();
		if (modNameFormat.isEmpty()) {
			return tooltip;
		}

		String modName = getModNameForIngredient(ingredient, ingredientHelper);
		if (tooltip.size() > 1) {
			String lastTooltipLine = tooltip.get(tooltip.size() - 1);
			lastTooltipLine = TextFormatting.getTextWithoutFormattingCodes(lastTooltipLine);
			if (modName.equals(lastTooltipLine)) {
				return tooltip;
			}
		}

		List<String> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(modNameFormat + modName);
		return tooltipCopy;
	}
}
