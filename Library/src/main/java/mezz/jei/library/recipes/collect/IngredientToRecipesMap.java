package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.IIngredientType;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngredientToRecipesMap<R> {
	private final IngredientUidIndex<ArrayList<R>> recipes = new IngredientUidIndex<>();

	public void addExact(R recipe, IIngredientType<?> ingredientType, Object uid) {
		recipes.computeExactIfAbsent(ingredientType, uid, ArrayList::new)
			.add(recipe);
	}

	@UnmodifiableView
	public List<R> get(IIngredientType<?> ingredientType, Object uid) {
		List<R> recipesForIngredient = recipes.get(ingredientType, uid).exact();
		if (recipesForIngredient == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(recipesForIngredient);
	}

	public void compact() {
		recipes.forEach((ingredientType, uid, buckets) -> {
			if (buckets.exact() != null) {
				buckets.exact().trimToSize();
			}
		});
	}
}
