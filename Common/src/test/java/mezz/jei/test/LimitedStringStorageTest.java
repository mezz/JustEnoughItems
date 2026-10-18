package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.search.LimitedStringStorage;
import mezz.jei.common.search.LimitedStringStorageBuilder;
import mezz.jei.common.search.LuceneSearchStorage;
import mezz.jei.common.search.RemovalFallbackSearchStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class LimitedStringStorageTest {
	@Test
	public void backingIndexStoresEachKeyOnce() {
		RecordingSearchStorage<Set<String>> backingStorage = new RecordingSearchStorage<>();
		LimitedStringStorage<String> limitedStringStorage = new LimitedStringStorage<>(backingStorage);

		limitedStringStorage.put("alpha", "first");
		limitedStringStorage.put("alpha", "second");

		Assertions.assertEquals(List.of("alpha"), backingStorage.keys);

		Set<String> results = new HashSet<>();
		limitedStringStorage.getSearchResults("alpha", results::addAll);

		Assertions.assertEquals(Set.of("first", "second"), results);
	}

	@Test
	public void customBuilderFactoryIsUsedForBackingIndex() {
		List<RecordingSearchStorageBuilder<?>> createdBuilders = new ArrayList<>();
		ISearchStorage<String> limitedStringStorage = createLimitedStringStorage(createdBuilders);

		Assertions.assertEquals(1, createdBuilders.size());
		RecordingSearchStorageBuilder<?> backingStorageBuilder = createdBuilders.get(0);
		Assertions.assertEquals(List.of("alpha"), backingStorageBuilder.storage.keys);

		Set<String> results = new HashSet<>();
		limitedStringStorage.getSearchResults("alpha", results::addAll);

		Assertions.assertEquals(Set.of("first", "second"), results);
	}

	@Test
	public void removesEmptyGroupsFromTheBackingIndexAndPreservesSharedMembers() {
		LuceneSearchStorage<Set<String>> backing = new LuceneSearchStorage<>();
		LimitedStringStorage<String> storage = new LimitedStringStorage<>(backing);
		String first = new String("equal");
		String second = new String("equal");
		storage.put("shared", first);
		storage.put("shared", second);
		storage.put("first only", first);
		storage.put("first alias", first);
		Assertions.assertEquals(3, allElements(backing).size());

		Assertions.assertTrue(storage.removeAll(List.of(first, first, new String("equal"))));
		Assertions.assertEquals(1, allElements(backing).size());
		Assertions.assertEquals(1, search(storage, "shared").size());
		Assertions.assertSame(second, search(storage, "shared").iterator().next());
		Assertions.assertTrue(search(backing, "first").isEmpty());

		storage.put("first only", first);
		Assertions.assertSame(first, search(storage, "first only").iterator().next());
		Assertions.assertTrue(search(storage, "first alias").isEmpty());
		Assertions.assertEquals(2, allElements(backing).size());
		Assertions.assertTrue(storage.removeAll(List.of(first, second)));
		Assertions.assertTrue(allElements(backing).isEmpty());
		Assertions.assertTrue(allElements(storage).isEmpty());
	}

	@Test
	public void leavesAllGroupsUnchangedWhenTheBackingStorageRequiresRebuilding() {
		ISearchStorageBuilderFactory factory = RemovalFallbackSearchStorage.wrap(RecordingSearchStorageBuilder::new);
		LimitedStringStorageBuilder<Object> builder = new LimitedStringStorageBuilder<>(factory);
		List<Object> removed = new ArrayList<>();
		for (int i = 0; i < 2048; i++) {
			Object value = new Object();
			removed.add(value);
			builder.put("unique " + i, value);
		}
		Object sharedMember = new Object();
		Object retained = new Object();
		removed.add(sharedMember);
		builder.put("shared", sharedMember);
		builder.put("shared", retained);
		ISearchStorage<Object> storage = builder.build();
		Set<Object> original = allElements(storage);

		Assertions.assertFalse(storage.removeAll(removed));
		Assertions.assertEquals(original, allElements(storage));
		Assertions.assertTrue(storage.removeAll(List.of(sharedMember)));
		Set<Object> remaining = allElements(storage);
		Assertions.assertFalse(remaining.contains(sharedMember));
		Assertions.assertTrue(remaining.contains(retained));
		Assertions.assertEquals(original.size() - 1, remaining.size());
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

	private static ISearchStorage<String> createLimitedStringStorage(List<RecordingSearchStorageBuilder<?>> createdBuilders) {
		LimitedStringStorageBuilder<String> limitedStringStorageBuilder = new LimitedStringStorageBuilder<>(new ISearchStorageBuilderFactory() {
			@Override
			public <T> ISearchStorageBuilder<T> create() {
				RecordingSearchStorageBuilder<T> builder = new RecordingSearchStorageBuilder<>();
				createdBuilders.add(builder);
				return builder;
			}
		});

		limitedStringStorageBuilder.put("alpha", "first");
		limitedStringStorageBuilder.put("alpha", "second");
		return limitedStringStorageBuilder.build();
	}

	private static class RecordingSearchStorage<T> implements ISearchStorage<T> {
		private final List<String> keys = new ArrayList<>();
		private final List<T> values = new ArrayList<>();

		@Override
		public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
			resultsConsumer.accept(values);
		}

		@Override
		public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
			resultsConsumer.accept(values);
		}

		@Override
		public void put(String key, T value) {
			keys.add(key);
			values.add(value);
		}

		@Override
		public String statistics() {
			return "RecordingSearchStorage";
		}
	}

	private static class RecordingSearchStorageBuilder<T> implements ISearchStorageBuilder<T> {
		private final RecordingSearchStorage<T> storage = new RecordingSearchStorage<>();

		@Override
		public void put(String key, T value) {
			storage.put(key, value);
		}

		@Override
		public ISearchStorage<T> build() {
			return storage;
		}
	}
}
