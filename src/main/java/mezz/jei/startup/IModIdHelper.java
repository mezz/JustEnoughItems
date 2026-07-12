package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;

public interface IModIdHelper {
	String getModNameForModId(String modId);

	@Nullable
	String getFormattedModNameForModId(String modId);

	default Set<String> getModAliases(String modId) {
		return Collections.emptySet();
	}

	<T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper);

	<T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper);

	@Nullable
	String getModNameTooltipFormatting();
}
