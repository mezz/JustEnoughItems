package mezz.jei.recipes;

import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeIngredientTable {
	private final Map<IRecipeCategory<?>, IngredientToRecipesMap<?>> map = new HashMap<>();

	public <V> void add(V recipe, IRecipeCategory<V> recipeCategory, Set<String> ingredientUids) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.computeIfAbsent(recipeCategory, k -> new IngredientToRecipesMap<>());
		ingredientToRecipesMap.add(recipe, ingredientUids);
	}

	public <V> List<V> get(IRecipeCategory<V> recipeCategory, String ingredientUid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.get(recipeCategory);
		if (ingredientToRecipesMap == null) {
			return List.of();
		}
		return ingredientToRecipesMap.get(ingredientUid);
	}
}
