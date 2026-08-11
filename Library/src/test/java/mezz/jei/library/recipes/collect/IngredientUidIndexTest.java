package mezz.jei.library.recipes.collect;

import mezz.jei.api.ingredients.IIngredientType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IngredientUidIndexTest {
	private static final IIngredientType<String> FIRST_TYPE = new TestIngredientType("first");
	private static final IIngredientType<String> SECOND_TYPE = new TestIngredientType("second");

	@Test
	void equalExactAndGroupingUidsShareOneCellWithoutMixingBuckets() {
		IngredientUidIndex<List<String>> index = new IngredientUidIndex<>();
		List<String> exact = index.computeExactIfAbsent(FIRST_TYPE, "same", ArrayList::new);
		List<String> grouping = index.computeGroupingIfAbsent(FIRST_TYPE, "same", ArrayList::new);

		MatchBuckets<List<String>> buckets = index.get(FIRST_TYPE, "same", "same");

		assertSame(exact, buckets.exact());
		assertSame(grouping, buckets.grouping());
	}

	@Test
	void ingredientTypesHaveIndependentUidRows() {
		IngredientUidIndex<List<String>> index = new IngredientUidIndex<>();
		index.computeExactIfAbsent(FIRST_TYPE, "same", ArrayList::new)
			.add("first");
		index.computeExactIfAbsent(SECOND_TYPE, "same", ArrayList::new)
			.add("second");

		assertEquals(List.of("first"), index.get(FIRST_TYPE, "same").exact());
		assertEquals(List.of("second"), index.get(SECOND_TYPE, "same").exact());
		assertNull(index.get(FIRST_TYPE, "same").grouping());
	}

	@Test
	void equalIngredientTypesShareOneUidRow() {
		IIngredientType<String> first = new TestIngredientType("same");
		IIngredientType<String> equalButDistinct = new TestIngredientType("same");
		assertNotSame(first, equalButDistinct);

		IngredientUidIndex<List<String>> index = new IngredientUidIndex<>();
		List<String> exact = index.computeExactIfAbsent(first, "uid", ArrayList::new);

		assertSame(exact, index.get(equalButDistinct, "uid").exact());
		assertSame(exact, index.computeExactIfAbsent(equalButDistinct, "uid", ArrayList::new));
	}

	private record TestIngredientType(String id) implements IIngredientType<String> {
		@Override
		public Class<? extends String> getIngredientClass() {
			return String.class;
		}
	}
}
