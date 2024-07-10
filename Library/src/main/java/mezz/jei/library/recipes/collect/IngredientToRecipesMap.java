package mezz.jei.library.recipes.collect;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientToRecipesMap<R> {
	private final Map<Object, List<R>> uidToRecipes = new HashMap<>();

	public void add(R recipe, Collection<Object> ingredientUids) {
		for (Object uid : ingredientUids) {
			List<R> recipes = uidToRecipes.computeIfAbsent(uid, k -> new ArrayList<>());
			recipes.add(recipe);
		}
	}

	@UnmodifiableView
	public List<R> get(Object ingredientUid) {
		List<R> recipes = uidToRecipes.get(ingredientUid);
		if (recipes == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(recipes);
	}
}
