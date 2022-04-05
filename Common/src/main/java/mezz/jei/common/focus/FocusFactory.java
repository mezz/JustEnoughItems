package mezz.jei.common.focus;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.util.ErrorUtil;

public class FocusFactory implements IFocusFactory {
	private final RegisteredIngredients registeredIngredients;

	public FocusFactory(RegisteredIngredients registeredIngredients) {
		this.registeredIngredients = registeredIngredients;
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		ErrorUtil.checkNotNull(mode, "mode");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		IIngredientType<V> ingredientType = registeredIngredients.getIngredientType(ingredient);
		return Focus.createFromApi(registeredIngredients, mode.toRole(), ingredientType, ingredient);
	}

	@Override
	public <V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient) {
		ErrorUtil.checkNotNull(role, "role");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(ingredient, "ingredient");
		return Focus.createFromApi(registeredIngredients, role, ingredientType, ingredient);
	}
}
