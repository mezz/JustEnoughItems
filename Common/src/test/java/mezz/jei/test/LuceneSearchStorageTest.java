package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.common.search.LimitedStringStorageBuilder;
import mezz.jei.common.search.LuceneSearchStorageBuilder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LRUQueryCache;
import org.apache.lucene.search.QueryCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@Isolated("Temporarily replaces Lucene's default query cache")
public class LuceneSearchStorageTest {
	@Test
	public void matchesLiteralSubstringsAcrossWordsAndUnicode() {
		LuceneSearchStorageBuilder<String> builder = new LuceneSearchStorageBuilder<>();
		builder.put("aer crystal", "crystal");
		builder.put("星の結晶 🧪", "unicode");
		builder.put("a*b?c\\d", "punctuation");
		builder.put("unrelated", "other");
		ISearchStorage<String> storage = builder.build();
		Assertions.assertEquals(Set.of("crystal"), search(storage, "er cry"));
		Assertions.assertEquals(Set.of("unicode"), search(storage, "結晶 🧪"));
		for (String literal : new String[]{"*", "?", "\\", "*b?c\\"}) {
			Assertions.assertEquals(Set.of("punctuation"), search(storage, literal));
		}
		Assertions.assertTrue(search(storage, "AER").isEmpty());
		Assertions.assertTrue(search(storage, "missing").isEmpty());
	}

	@Test
	public void returnsAllMatchingValuesWithoutRankingOrTruncation() {
		LuceneSearchStorageBuilder<Object> builder = new LuceneSearchStorageBuilder<>();
		Set<Object> expected = Collections.newSetFromMap(new IdentityHashMap<>());
		for (int i = 0; i < 1500; i++) {
			Object value = new Object();
			expected.add(value);
			builder.put("shared key " + i, value);
			builder.put("another shared key " + i, value);
		}
		ISearchStorage<Object> storage = builder.build();
		Assertions.assertEquals(expected, search(storage, "shared"));
		Assertions.assertEquals(expected, allElements(storage));
	}

	@Test
	public void preservesValueMappingAcrossRuntimeBatchesAndSegmentMerges() {
		LuceneSearchStorageBuilder<String> builder = new LuceneSearchStorageBuilder<>();
		builder.put("original", "original value");
		ISearchStorage<String> storage = builder.build();
		for (int batch = 0; batch < 30; batch++) {
			for (int i = 0; i < 20; i++) {
				String key = "entry[" + (batch * 20 + i) + "]";
				storage.put(key, key);
			}
			Assertions.assertEquals(1 + (batch + 1) * 20, allElements(storage).size());
			Assertions.assertEquals(Set.of("original value"), search(storage, "original"));
			Assertions.assertEquals(Set.of("entry[" + batch * 20 + "]"), search(storage, "[" + batch * 20 + "]"));
		}
		for (int i = 0; i < 600; i++) {
			Assertions.assertEquals(Set.of("entry[" + i + "]"), search(storage, "[" + i + "]"));
		}
	}

	@Test
	public void supportsEmptyIndexesAndRuntimeOnlyIngredients() {
		ISearchStorage<String> storage = new LuceneSearchStorageBuilder<String>().build();
		Assertions.assertTrue(search(storage, "crystal").isEmpty());
		Assertions.assertTrue(allElements(storage).isEmpty());
		storage.put("aer crystal", "aer");
		storage.put("ignis crystal", "ignis");
		Assertions.assertEquals(Set.of("aer", "ignis"), search(storage, "crystal"));
	}

	@Test
	public void skipsStringsAboveTheUtf8TermLimitWithoutRemovingTheirValues() {
		String atLimit = "結".repeat(10_922);
		String aboveLimit = atLimit + "x";
		LuceneSearchStorageBuilder<String> builder = new LuceneSearchStorageBuilder<>();
		builder.put(atLimit, "boundary");
		builder.put(aboveLimit, "long");
		builder.put("valid alias", "long");
		builder.put(aboveLimit, "only oversized");
		ISearchStorage<String> storage = builder.build();
		Assertions.assertEquals(Set.of("boundary"), search(storage, "結"));
		Assertions.assertTrue(search(storage, "x").isEmpty());
		Assertions.assertTrue(search(storage, aboveLimit).isEmpty());
		Assertions.assertEquals(Set.of("long"), search(storage, "alias"));
		Assertions.assertEquals(Set.of("boundary", "long", "only oversized"), allElements(storage));
	}

	@Test
	public void skipsOversizedRuntimeStringsAndKeepsOtherSearchKeys() {
		LuceneSearchStorageBuilder<String> builder = new LuceneSearchStorageBuilder<>();
		builder.put("original", "original");
		ISearchStorage<String> storage = builder.build();
		storage.put("x".repeat(32_767) + "skipped", "runtime");
		storage.put("runtime alias", "runtime");
		storage.put("another ingredient", "another");
		Assertions.assertTrue(search(storage, "skipped").isEmpty());
		Assertions.assertEquals(Set.of("runtime"), search(storage, "alias"));
		Assertions.assertEquals(Set.of("original"), search(storage, "original"));
		Assertions.assertEquals(Set.of("another"), search(storage, "another"));
		Assertions.assertEquals(Set.of("original", "runtime", "another"), allElements(storage));
	}

	@Test
	public void updatesSharedKeysAndIndexesNewKeysInLimitedStringStorage() {
		LimitedStringStorageBuilder<String> builder = new LimitedStringStorageBuilder<>(LuceneSearchStorageBuilder::new);
		builder.put("shared", "first");
		ISearchStorage<String> storage = builder.build();
		storage.put("shared", "second");
		storage.put("new key", "third");
		Assertions.assertEquals(Set.of("first", "second"), search(storage, "shared"));
		Assertions.assertEquals(Set.of("third"), search(storage, "new"));
		Assertions.assertEquals(Set.of("first", "second", "third"), allElements(storage));
	}

	@Test
	public void doesNotPopulateLucenesSharedCache() {
		QueryCache previousCache = IndexSearcher.getDefaultQueryCache();
		LRUQueryCache sharedCache = new LRUQueryCache(128, 4L * 1024 * 1024);
		IndexSearcher.setDefaultQueryCache(sharedCache);
		try {
			LuceneSearchStorageBuilder<String> builder = new LuceneSearchStorageBuilder<>();
			// Lucene only caches queries for sufficiently large segments.
			for (int i = 0; i < 20_000; i++) {
				builder.put("cached ingredient " + i, "initial");
			}
			ISearchStorage<String> storage = builder.build();
			for (int i = 0; i < 10; i++) {
				Assertions.assertEquals(Set.of("initial"), search(storage, "cached"));
			}
			Assertions.assertEquals(0, sharedCache.getCacheCount());

			storage.put("cached runtime ingredient", "added");
			for (int i = 0; i < 10; i++) {
				Assertions.assertEquals(Set.of("initial", "added"), search(storage, "cached"));
			}
			Assertions.assertEquals(0, sharedCache.getCacheCount());
		} finally {
			IndexSearcher.setDefaultQueryCache(previousCache);
		}
	}

	private static <T> Set<T> search(ISearchStorage<T> storage, String token) {
		Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		storage.getSearchResults(token, results::addAll);
		return results;
	}

	private static <T> Set<T> allElements(ISearchStorage<T> storage) {
		Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		storage.getAllElements(results::addAll);
		return results;
	}
}
