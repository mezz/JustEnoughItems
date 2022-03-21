package mezz.jei.recipes;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientToRecipesMap<R> {
	private final Map<String, List<R>> uidToRecipes = new HashMap<>();

	public void add(R recipe, List<String> ingredientUids) {
		for (String uid : ingredientUids) {
			List<R> recipes = uidToRecipes.computeIfAbsent(uid, k -> new ArrayList<>());
			recipes.add(recipe);
		}
	}

	@UnmodifiableView
	public List<R> get(String ingredientUid) {
		List<R> recipes = uidToRecipes.get(ingredientUid);
		if (recipes == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(recipes);
	}
}
