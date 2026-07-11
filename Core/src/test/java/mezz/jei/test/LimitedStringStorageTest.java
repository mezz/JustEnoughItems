package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.core.search.LimitedStringStorage;
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
	public void customFactoryIsUsedForBackingIndex() {
		List<RecordingSearchStorage<?>> createdStorages = new ArrayList<>();
		LimitedStringStorage<String> limitedStringStorage = new LimitedStringStorage<>(new ISearchStorageFactory() {
			@Override
			public <T> ISearchStorage<T> createSearchStorage() {
				RecordingSearchStorage<T> storage = new RecordingSearchStorage<>();
				createdStorages.add(storage);
				return storage;
			}
		});

		limitedStringStorage.put("alpha", "first");
		limitedStringStorage.put("alpha", "second");

		Assertions.assertEquals(1, createdStorages.size());
		RecordingSearchStorage<?> backingStorage = createdStorages.get(0);
		Assertions.assertEquals(List.of("alpha"), backingStorage.keys);

		Set<String> results = new HashSet<>();
		limitedStringStorage.getSearchResults("alpha", results::addAll);

		Assertions.assertEquals(Set.of("first", "second"), results);
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
}
