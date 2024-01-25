package mezz.jei.ingredients;

import com.google.common.collect.ImmutableSetMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
	public <T> List<ITextComponent> addModNameToIngredientTooltip(List<ITextComponent> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (!isDisplayingModNameEnabled()) {
			return tooltip;
		}
		String modId = ingredientHelper.getDisplayModId(ingredient);
		String modName = getFormattedModNameForModId(modId);
		List<ITextComponent> tooltipCopy = new ArrayList<>(tooltip);
		tooltipCopy.add(new StringTextComponent(modName));
		return tooltipCopy;
	}
}
