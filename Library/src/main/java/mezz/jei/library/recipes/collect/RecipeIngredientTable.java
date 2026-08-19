package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.types.IRecipeType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeIngredientTable {
	private final Map<IRecipeType<?>, IngredientToRecipesMap<?>> map = new HashMap<>();

	public <V> void addExact(V recipe, IRecipeType<V> recipeType, IIngredientType<?> ingredientType, Object uid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.computeIfAbsent(recipeType, k -> new IngredientToRecipesMap<>());
		ingredientToRecipesMap.addExact(recipe, ingredientType, uid);
	}

	public <V> void addGrouping(V recipe, IRecipeType<V> recipeType, IIngredientType<?> ingredientType, Object uid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.computeIfAbsent(recipeType, k -> new IngredientToRecipesMap<>());
		ingredientToRecipesMap.addGrouping(recipe, ingredientType, uid);
	}

	@UnmodifiableView
	public <V> List<V> get(IRecipeType<V> recipeType, IIngredientType<?> ingredientType, Object exactUid, Object groupingUid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.get(recipeType);
		if (ingredientToRecipesMap == null) {
			return List.of();
		}
		return ingredientToRecipesMap.get(ingredientType, exactUid, groupingUid);
	}

	public void compact() {
		map.values().forEach(IngredientToRecipesMap::compact);
	}
}
