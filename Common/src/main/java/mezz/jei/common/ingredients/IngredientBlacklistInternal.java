package mezz.jei.common.ingredients;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;

public class IngredientBlacklistInternal implements IIngredientManager.IIngredientListener {
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

	@Override
	public ResourceLocation getUid() {
		return new ResourceLocation(ModIds.JEI_ID, "ingredient_blacklist_internal");
	}

	@Override
	public <V> void onIngredientsAdded(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			removeIngredientFromBlacklist(ingredient, ingredientHelper);
		}
	}

	@Override
	public <V> void onIngredientsRemoved(IIngredientHelper<V> ingredientHelper, Collection<ITypedIngredient<V>> ingredients) {
		for (ITypedIngredient<V> ingredient : ingredients) {
			addIngredientToBlacklist(ingredient, ingredientHelper);
		}
	}
}
