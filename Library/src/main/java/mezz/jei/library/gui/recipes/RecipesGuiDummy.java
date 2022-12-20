package mezz.jei.library.gui.recipes;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IRecipesGui;

import java.util.List;
import java.util.Optional;

public class RecipesGuiDummy implements IRecipesGui {
	public static final IRecipesGui INSTANCE = new RecipesGuiDummy();

	public RecipesGuiDummy() {

	}

	@Override
	public void show(List<IFocus<?>> focuses) {

	}

	@Override
	public void showTypes(List<RecipeType<?>> recipeTypes) {

	}

	@Override
	public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		return Optional.empty();
	}
}
