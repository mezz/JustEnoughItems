package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.category.IRecipeCategory;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class StaticFocusedRecipes<T> implements IFocusedRecipes<T> {
	private final IRecipeCategory<T> recipeCategory;
	private final List<T> recipes;

	public StaticFocusedRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes) {
		this.recipeCategory = recipeCategory;
		this.recipes = List.copyOf(recipes);
	}

	@Override
	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	@Override
	public @Unmodifiable List<T> getRecipes() {
		return recipes;
	}
}
