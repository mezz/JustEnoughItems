package mezz.jei.gui.search;

import mezz.jei.api.search.ISearchStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ElementSearchTest {
	@Test
	public void blankStringIsNotPutIntoSearchStorage() {
		RecordingSearchStorage<Object> storage = new RecordingSearchStorage<>();
		Object element = new Object();

		ElementSearch.putIfNotBlank(storage, "", element);
		ElementSearch.putIfNotBlank(storage, " \t ", element);
		ElementSearch.putIfNotBlank(storage, " alpha ", element);

		Assertions.assertEquals(List.of("alpha"), storage.keys);
		Assertions.assertEquals(List.of(element), storage.values);
	}

	private static class RecordingSearchStorage<T> implements ISearchStorage<T> {
		private final List<String> keys = new ArrayList<>();
		private final List<T> values = new ArrayList<>();

		@Override
		public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {

		}

		@Override
		public void getAllElements(Consumer<Collection<T>> resultsConsumer) {

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
