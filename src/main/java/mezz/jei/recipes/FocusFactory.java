package mezz.jei.recipes;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.Focus;

public class FocusFactory implements IFocusFactory {
	private final IIngredientManager ingredientManager;

	public FocusFactory(IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
	}

	@SuppressWarnings("removal")
	@Override
	public <V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient) {
		IIngredientType<V> ingredientType = ingredientManager.getIngredientType(ingredient);
		return Focus.createFromApi(ingredientManager, mode.toRole(), ingredientType, ingredient);
	}

	@Override
	public <V> IFocus<V> createFocus(RecipeIngredientRole role, IIngredientType<V> ingredientType, V ingredient) {
		return Focus.createFromApi(ingredientManager, role, ingredientType, ingredient);
	}
}
