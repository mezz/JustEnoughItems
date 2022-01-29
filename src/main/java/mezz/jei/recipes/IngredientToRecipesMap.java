package mezz.jei.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IngredientToRecipesMap<R> {
	private final Map<String, List<R>> uidToRecipes = new HashMap<>();

	public void add(R recipe, Set<String> ingredientUids) {
		for (String uid : ingredientUids) {
			List<R> recipes = uidToRecipes.computeIfAbsent(uid, k -> new ArrayList<>());
			recipes.add(recipe);
		}
	}

	public List<R> get(String ingredientUid) {
		List<R> recipes = uidToRecipes.get(ingredientUid);
		if (recipes == null) {
			return List.of();
		}
		return Collections.unmodifiableList(recipes);
	}
}
