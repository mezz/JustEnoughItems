package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.IIngredientType;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class IngredientToRecipesMap<R> {
	private final IngredientUidIndex<ArrayList<R>> recipes = new IngredientUidIndex<>();

	public void addExact(R recipe, IIngredientType<?> ingredientType, Object uid) {
		recipes.computeExactIfAbsent(ingredientType, uid, ArrayList::new)
			.add(recipe);
	}

	public void addGrouping(R recipe, IIngredientType<?> ingredientType, Object uid) {
		recipes.computeGroupingIfAbsent(ingredientType, uid, ArrayList::new)
			.add(recipe);
	}

	@UnmodifiableView
	public List<R> get(IIngredientType<?> ingredientType, Object exactUid, Object groupingUid) {
		MatchBuckets<ArrayList<R>> buckets = recipes.get(ingredientType, exactUid, groupingUid);
		if (buckets.exact() == null) {
			return unmodifiable(buckets.grouping());
		}
		if (buckets.grouping() == null) {
			return Collections.unmodifiableList(buckets.exact());
		}
		return Stream.concat(buckets.exact().stream(), buckets.grouping().stream())
			.distinct()
			.toList();
	}

	public void compact() {
		recipes.forEach((ingredientType, uid, buckets) -> {
			if (buckets.exact() != null) {
				buckets.exact().trimToSize();
			}
			if (buckets.grouping() != null) {
				buckets.grouping().trimToSize();
			}
		});
	}

	private static <R> List<R> unmodifiable(@Nullable ArrayList<R> recipes) {
		if (recipes == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(recipes);
	}
}
