package mezz.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredientBlacklistInternal {
	private final Set<String> ingredientBlacklist = new HashSet<>();

	public <V> void addIngredientToBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
		String uniqueName = ingredientHelper.getUniqueId(ingredient);
		ingredientBlacklist.add(uniqueName);
	}

	public <V> void removeIngredientFromBlacklist(V ingredient, IIngredientHelper<V> ingredientHelper) {
		String uniqueName = ingredientHelper.getUniqueId(ingredient);
		ingredientBlacklist.remove(uniqueName);
	}

	public <V> boolean isIngredientBlacklisted(V ingredient, IIngredientHelper<V> ingredientHelper) {
		return isIngredientBlacklistedByApi(ingredient, ingredientHelper) ||
			Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}

	public <V> boolean isIngredientBlacklistedByConfig(V ingredient, IIngredientHelper<V> ingredientHelper) {
		return Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper);
	}

	public <V> boolean isIngredientBlacklistedByApi(V ingredient, IIngredientHelper<V> ingredientHelper) {
		List<String> uids = IngredientInformation.getUniqueIdsWithWildcard(ingredientHelper, ingredient);

		for (String uid : uids) {
			if (ingredientBlacklist.contains(uid)) {
				return true;
			}
		}

		return false;
	}
}
