package mezz.jei.library.recipes.collect;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.ingredients.IIngredientType;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Indexes exact and grouping values by ingredient type and UID.
 */
final class IngredientUidIndex<V> {
	private final Map<IIngredientType<?>, Map<Object, MatchBuckets<V>>> rows = new HashMap<>();

	public MatchBuckets<V> get(IIngredientType<?> ingredientType, Object uid) {
		Map<Object, MatchBuckets<V>> row = rows.get(ingredientType);
		if (row == null) {
			return MatchBuckets.empty();
		}
		return row.getOrDefault(uid, MatchBuckets.empty());
	}

	/**
	 * Gets exact and grouping values, using one UID lookup when both UIDs are equal.
	 */
	public MatchBuckets<V> get(IIngredientType<?> ingredientType, Object exactUid, Object groupingUid) {
		Map<Object, MatchBuckets<V>> row = rows.get(ingredientType);
		if (row == null) {
			return MatchBuckets.empty();
		}

		MatchBuckets<V> exactBuckets = row.getOrDefault(exactUid, MatchBuckets.empty());
		if (Objects.equals(exactUid, groupingUid)) {
			return exactBuckets;
		}

		MatchBuckets<V> groupingBuckets = row.getOrDefault(groupingUid, MatchBuckets.empty());
		return new MatchBuckets<>(exactBuckets.exact(), groupingBuckets.grouping());
	}

	public V computeExactIfAbsent(IIngredientType<?> ingredientType, Object uid, Supplier<V> valueSupplier) {
		Map<Object, MatchBuckets<V>> row = getOrCreateRow(ingredientType);
		MatchBuckets<V> buckets = row.getOrDefault(uid, MatchBuckets.empty());
		V value = buckets.exact();
		if (value == null) {
			value = valueSupplier.get();
			row.put(uid, buckets.withExact(value));
		}
		return value;
	}

	public V computeGroupingIfAbsent(IIngredientType<?> ingredientType, Object uid, Supplier<V> valueSupplier) {
		Map<Object, MatchBuckets<V>> row = getOrCreateRow(ingredientType);
		MatchBuckets<V> buckets = row.getOrDefault(uid, MatchBuckets.empty());
		V value = buckets.grouping();
		if (value == null) {
			value = valueSupplier.get();
			row.put(uid, buckets.withGrouping(value));
		}
		return value;
	}

	public void forEach(EntryConsumer<V> consumer) {
		rows.forEach((ingredientType, row) -> row.forEach((uid, buckets) -> consumer.accept(ingredientType, uid, buckets)));
	}

	private Map<Object, MatchBuckets<V>> getOrCreateRow(IIngredientType<?> ingredientType) {
		return rows.computeIfAbsent(ingredientType, type -> new Object2ObjectOpenHashMap<>());
	}

	@FunctionalInterface
	public interface EntryConsumer<V> {
		void accept(IIngredientType<?> ingredientType, Object uid, MatchBuckets<V> buckets);
	}
}

record MatchBuckets<V>(
	@Nullable V exact,
	@Nullable V grouping
) {
	private static final MatchBuckets<?> EMPTY = new MatchBuckets<>(null, null);

	@SuppressWarnings("unchecked")
	public static <V> MatchBuckets<V> empty() {
		return (MatchBuckets<V>) EMPTY;
	}

	public MatchBuckets<V> withExact(V exact) {
		return new MatchBuckets<>(exact, grouping);
	}

	public MatchBuckets<V> withGrouping(V grouping) {
		return new MatchBuckets<>(exact, grouping);
	}
}
