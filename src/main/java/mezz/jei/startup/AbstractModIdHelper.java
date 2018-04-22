package mezz.jei.startup;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public abstract class AbstractModIdHelper implements IModIdHelper {
	@Override
	public <T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String modId = ingredientHelper.getModId(ingredient);
		return getModNameForModId(modId);
	}

	@Override
	public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (Config.isDebugModeEnabled()) {
			tooltip.add(TextFormatting.GRAY + "JEI Debug ingredient info:");
			tooltip.add(TextFormatting.GRAY + ingredientHelper.getErrorInfo(ingredient));
		}
		if (Config.isModNameFormatOverrideActive() && ingredient instanceof ItemStack) { // we detected that another mod is adding the mod name already
			return tooltip;
		}
		String modNameFormat = Config.getModNameFormat();
		if (modNameFormat.isEmpty()) {
			return tooltip;
		}

		String modId = ingredientHelper.getDisplayModId(ingredient);
		String modName = getFormattedModNameForModId(modId);
		List<String> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(modName);
		return tooltipCopy;
	}
}
