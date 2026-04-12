package mezz.jei.common.ingredients.group;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;

public sealed interface IIngredientGroupSelector permits DynamicSelector, IngredientsSelector, TagSelector, RegExpSelector {

	IngredientGroupType getType();

	boolean test(ITypedIngredient<?> ingredient, IIngredientManager ingredientManager);

}
