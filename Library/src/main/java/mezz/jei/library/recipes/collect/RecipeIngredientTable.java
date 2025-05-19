package mezz.jei.library.recipes.collect;

import mezz.jei.api.recipe.types.IRecipeType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeIngredientTable {
	private final Map<IRecipeType<?>, IngredientToRecipesMap<?>> map = new HashMap<>();

	public <V> void add(V recipe, IRecipeType<V> recipeType, Collection<Object> ingredientUids) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.computeIfAbsent(recipeType, k -> new IngredientToRecipesMap<>());
		ingredientToRecipesMap.add(recipe, ingredientUids);
	}

	@UnmodifiableView
	public <V> List<V> get(IRecipeType<V> recipeType, Object ingredientUid) {
		@SuppressWarnings("unchecked")
		IngredientToRecipesMap<V> ingredientToRecipesMap = (IngredientToRecipesMap<V>) this.map.get(recipeType);
		if (ingredientToRecipesMap == null) {
			return List.of();
		}
		return ingredientToRecipesMap.get(ingredientUid);
	}

	public void compact() {
		map.values().forEach(IngredientToRecipesMap::compact);
	}
}
