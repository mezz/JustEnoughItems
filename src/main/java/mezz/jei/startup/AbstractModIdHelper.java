package mezz.jei.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSetMultimap;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;

public abstract class AbstractModIdHelper implements IModIdHelper {
	private ImmutableSetMultimap<String, String> modAliases = ImmutableSetMultimap.of();

	@Override
	public Set<String> getModAliases(String modId) {
		return modAliases.get(modId);
	}

	public void setModAliases(ImmutableSetMultimap<String, String> modAliases) {
		this.modAliases = modAliases;
	}

	@Override
	public <T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper) {
		String modId = ingredientHelper.getResourceLocation(ingredient).getNamespace();
		return getModNameForModId(modId);
	}

	@Override
	public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		String modNameFormat = Config.getModNameFormat();
		if (modNameFormat.isEmpty()) {
			return tooltip;
		}

		String modId = ingredientHelper.getDisplayModId(ingredient);
		String modName = getFormattedModNameForModId(modId);
		if (modName == null) {
			return tooltip;
		}
		List<String> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(modName);
		return tooltipCopy;
	}
}
