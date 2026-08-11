package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeIngredientTable {
	private final Map<RecipeType<?>, IngredientToRecipesMap<?>> map = new HashMap<>();

	public <V> void addExact(V recipe, RecipeType<V> recipeType, IIngredientType<?> ingredientType, Object uid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.computeIfAbsent(recipeType, k -> new IngredientToRecipesMap<>());
		ingredientToRecipesMap.addExact(recipe, ingredientType, uid);
	}

	@UnmodifiableView
	public <V> List<V> get(RecipeType<V> recipeType, IIngredientType<?> ingredientType, Object uid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.get(recipeType);
		if (ingredientToRecipesMap == null) {
			return List.of();
		}
		return ingredientToRecipesMap.get(ingredientType, uid);
	}

	public void compact() {
		map.values().forEach(IngredientToRecipesMap::compact);
	}
}
