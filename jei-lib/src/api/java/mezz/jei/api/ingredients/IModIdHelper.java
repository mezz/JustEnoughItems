package mezz.jei.api.ingredients;

import java.util.List;

public interface IModIdHelper {
	String getModNameForModId(String modId);

	boolean isDisplayingModNameEnabled();

	String getFormattedModNameForModId(String modId);

	<T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper);
}
