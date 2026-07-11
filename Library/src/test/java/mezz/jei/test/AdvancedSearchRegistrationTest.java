package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.common.search.GeneralizedSuffixTreeSearchStorage;
import mezz.jei.library.load.registration.AdvancedSearchRegistration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Consumer;

public class AdvancedSearchRegistrationTest {
	@Test
	public void defaultStorageUsesGeneralizedSuffixTreeSearchStorage() {
		AdvancedSearchRegistration searchRegistration = new AdvancedSearchRegistration();

		ISearchStorage<?> searchStorage = searchRegistration.getSearchStorageSupplier().get();

		Assertions.assertInstanceOf(GeneralizedSuffixTreeSearchStorage.class, searchStorage);
	}

	@Test
	public void customSupplierIsUsedByFactory() {
		RecordingSearchStorage<Object> storage = new RecordingSearchStorage<>();
		AdvancedSearchRegistration searchRegistration = new AdvancedSearchRegistration();
		searchRegistration.replaceSearchStorage(() -> storage);

		ISearchStorage<?> searchStorage = searchRegistration.getSearchStorageSupplier().get();

		Assertions.assertSame(storage, searchStorage);
	}

	private static class RecordingSearchStorage<T> implements ISearchStorage<T> {
		@Override
		public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {

		}

		@Override
		public void getAllElements(Consumer<Collection<T>> resultsConsumer) {

		}

		@Override
		public void put(String key, T value) {

		}

		@Override
		public String statistics() {
			return "RecordingSearchStorage";
		}
	}
}
