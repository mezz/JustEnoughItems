package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.common.search.LimitedStringStorage;
import mezz.jei.common.search.LimitedStringStorageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
		RecordingSearchStorageBuilder<?> backingStorageBuilder = createdBuilders.getFirst();
		Assertions.assertEquals(List.of("alpha"), backingStorageBuilder.storage.keys);

		Set<String> results = new HashSet<>();
		limitedStringStorage.getSearchResults("alpha", results::addAll);

		Assertions.assertEquals(Set.of("first", "second"), results);
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
