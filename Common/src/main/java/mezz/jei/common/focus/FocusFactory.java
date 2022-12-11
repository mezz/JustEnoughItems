package mezz.jei.common.focus;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.ErrorUtil;

public class FocusFactory implements IFocusFactory {
	private final IRegisteredIngredients registeredIngredients;

	public FocusFactory(IRegisteredIngredients registeredIngredients) {
		this.registeredIngredients = registeredIngredients;
	}

	@Override
	public <V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient) {
		ErrorUtil.checkNotNull(role, "role");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return Focus.createFromApi(registeredIngredients, role, ingredientType, ingredient);
	}
}
