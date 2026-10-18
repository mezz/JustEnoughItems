package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.common.search.LuceneSearchStorageBuilder;
import mezz.jei.common.search.RemovalFallbackSearchStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

public class RemovalFallbackSearchStorageTest {
	@Test
	public void removesAllKeysByIdentityWithoutRevivingOldMatches() {
		ISearchStorageBuilder<String> builder = RemovalFallbackSearchStorage.wrap(PrefixStorage::new).create("names");
		String first = new String("equal");
		String second = new String("equal");
		builder.put("old first", first);
		builder.put("old shared", first);
		builder.put("old shared", second);
		ISearchStorage<String> storage = builder.build();
		storage.put("pending", first);

		Assertions.assertTrue(storage.removeAll(List.of(first, first, new String("equal"))));
		storage.put("new first", first);
		Assertions.assertTrue(search(storage, "old first").isEmpty());
		Assertions.assertTrue(search(storage, "pending").isEmpty());
		Assertions.assertSame(second, search(storage, "old shared").iterator().next());
		Assertions.assertSame(first, search(storage, "new").iterator().next());
		Assertions.assertTrue(search(storage, "first").isEmpty());

		Assertions.assertTrue(storage.removeAll(List.of(first, second)));
		Set<String> all = Collections.newSetFromMap(new IdentityHashMap<>());
		storage.getAllElements(all::addAll);
		Assertions.assertTrue(all.isEmpty());
		storage.put("restored", first);
		Assertions.assertTrue(search(storage, "new").isEmpty());
		Assertions.assertSame(first, search(storage, "restored").iterator().next());
	}

	@Test
	public void requestsRebuildWithoutChangesForLargeUnsupportedRemoval() {
		ISearchStorageBuilder<Object> builder = RemovalFallbackSearchStorage.wrap(PrefixStorage::new).create();
		List<Object> values = new ArrayList<>();
		for (int i = 0; i < 2048; i++) {
			Object value = new Object();
			values.add(value);
			builder.put("old " + i, value);
		}
		ISearchStorage<Object> storage = builder.build();

		Assertions.assertFalse(storage.removeAll(values));
		Assertions.assertEquals(Set.copyOf(values), search(storage, "old"));
	}

	@Test
	public void usesNativeDeletionWithoutRequestingRebuild() {
		ISearchStorageBuilder<Object> builder = RemovalFallbackSearchStorage.wrap(LuceneSearchStorageBuilder::new).create();
		List<Object> values = new ArrayList<>();
		for (int i = 0; i < 2048; i++) {
			Object value = new Object();
			values.add(value);
			builder.put("old " + i, value);
		}
		ISearchStorage<Object> storage = builder.build();

		Assertions.assertTrue(storage.removeAll(values));
		Assertions.assertTrue(search(storage, "old").isEmpty());
		storage.put("new", values.get(0));
		Assertions.assertEquals(Set.of(values.get(0)), search(storage, "new"));
	}

	private static <T> Set<T> search(ISearchStorage<T> storage, String token) {
		Set<T> results = Collections.newSetFromMap(new IdentityHashMap<>());
		storage.getSearchResults(token, results::addAll);
		return results;
	}

	private static class PrefixStorage<T> implements ISearchStorage<T>, ISearchStorageBuilder<T> {
		private final TreeMap<String, List<T>> entries = new TreeMap<>();

		@Override
		public void put(String key, T value) {
			entries.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
		}

		@Override
		public ISearchStorage<T> build() {
			return this;
		}

		@Override
		public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
			for (Map.Entry<String, List<T>> entry : entries.tailMap(token).entrySet()) {
				if (!entry.getKey().startsWith(token)) {
					break;
				}
				resultsConsumer.accept(entry.getValue());
			}
		}

		@Override
		public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
			entries.values().forEach(resultsConsumer);
		}

		@Override
		public String statistics() {
			return "PrefixStorage";
		}
	}
}
