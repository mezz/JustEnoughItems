package mezz.jei.library.recipes.collect;

import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeIngredientTable {
	private final Map<RecipeType<?>, IngredientToRecipesMap<?>> map = new HashMap<>();

	public <V> void add(V recipe, RecipeType<V> recipeType, List<String> ingredientUids) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.computeIfAbsent(recipeType, k -> new IngredientToRecipesMap<>());
		ingredientToRecipesMap.add(recipe, ingredientUids);
	}

	@UnmodifiableView
	public <V> List<V> get(RecipeType<V> recipeType, String ingredientUid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.get(recipeType);
		if (ingredientToRecipesMap == null) {
			return List.of();
		}
		return ingredientToRecipesMap.get(ingredientUid);
	}
}
