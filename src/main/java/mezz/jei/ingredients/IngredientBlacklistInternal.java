package mezz.jei.ingredients;

import java.util.HashSet;
import java.util.Set;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;

public class IngredientBlacklistInternal {
	private final Set<String> ingredientBlacklist = new HashSet<>();

	public <V> void addIngredientToBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		ingredientBlacklist.add(uniqueName);
	}

	public <V> void removeIngredientFromBlacklist(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uniqueName = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		ingredientBlacklist.remove(uniqueName);
	}

	public <V> boolean isIngredientBlacklistedByApi(ITypedIngredient<V> typedIngredient, IIngredientHelper<V> ingredientHelper) {
		V ingredient = typedIngredient.getIngredient();
		String uid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
		String uidWild = ingredientHelper.getWildcardId(ingredient);

		if (uid.equals(uidWild)) {
			return ingredientBlacklist.contains(uid);
		}
		return ingredientBlacklist.contains(uid) || ingredientBlacklist.contains(uidWild);
	}

}
