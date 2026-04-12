package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;

import java.util.List;

public record IngredientsSelector(List<ITypedIngredient<?>> ingredients) implements IIngredientGroupSelector {

	@Override
	public IngredientGroupType getType() {
		return IngredientGroupType.INGREDIENTS;
	}

	@Override
	public boolean test(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		for (ITypedIngredient<?> candidate : ingredients) {
			if (ingredientMatches(candidate, ingredient, ingredientManager)) {
				return true;
			}
		}
		return false;
	}

	private static <T> boolean ingredientMatches(ITypedIngredient<?> candidate, ITypedIngredient<?> ingredient, IIngredientManager ingredientManager) {
		if (!candidate.getType().equals(ingredient.getType())) {
			return false;
		}
		@SuppressWarnings("unchecked")
		IIngredientType<T> type = (IIngredientType<T>) candidate.getType();
		IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);
		@SuppressWarnings("unchecked")
		T candidateRaw = (T) candidate.getIngredient();
		@SuppressWarnings("unchecked")
		T ingredientRaw = (T) ingredient.getIngredient();
		Object candidateUid = helper.getUid(candidateRaw, UidContext.Ingredient);
		Object ingredientUid = helper.getUid(ingredientRaw, UidContext.Ingredient);
		return candidateUid.equals(ingredientUid);
	}
}
