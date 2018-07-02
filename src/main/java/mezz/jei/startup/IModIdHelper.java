package mezz.jei.startup;

import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;

import javax.annotation.Nullable;

public interface IModIdHelper {
	String getModNameForModId(String modId);

	@Nullable
	String getFormattedModNameForModId(String modId);

	<T> String getModNameForIngredient(T ingredient, IIngredientHelper<T> ingredientHelper);

	<T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper);

	@Nullable
	String getModNameTooltipFormatting();
}
