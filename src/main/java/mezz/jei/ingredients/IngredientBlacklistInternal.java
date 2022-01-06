package mezz.jei.ingredients;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class IngredientBlacklistInternal {
	private final Set<String> ingredientBlacklist = new HashSet<>();

	public <V> void addIngredientToBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		ingredientBlacklist.add(uniqueName);
	}

	public <V> void removeIngredientFromBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		ingredientBlacklist.remove(uniqueName);
	}

	public <V> boolean isIngredientBlacklistedByApi(V ingredient, IIngredientHelper<V> ingredientHelper) {
		List<String> uids = IngredientInformationUtil.getUniqueIdsWithWildcard(ingredientHelper, ingredient, UidContext.Ingredient);

		return uids.stream()
			.anyMatch(ingredientBlacklist::contains);
	}
}
