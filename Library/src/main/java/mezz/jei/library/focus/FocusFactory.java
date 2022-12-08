package mezz.jei.library.focus;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.ErrorUtil;

import java.util.Collection;

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

	@Override
	public <V> IFocus<V> createFocus(RecipeIngredientRole role, ITypedIngredient<V> typedIngredient) {
		ErrorUtil.checkNotNull(role, "role");
		ErrorUtil.checkNotNull(typedIngredient, "typedIngredient");
		return Focus.createFromApi(registeredIngredients, role, typedIngredient);
	}

	@Override
	public IFocusGroup createFocusGroup(Collection<? extends IFocus<?>> focuses) {
		return FocusGroup.create(focuses, registeredIngredients);
	}
}
