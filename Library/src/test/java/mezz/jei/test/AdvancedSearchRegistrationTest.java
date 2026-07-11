package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;
import mezz.jei.library.load.PluginLoader;
import mezz.jei.library.load.registration.AdvancedSearchRegistration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class AdvancedSearchRegistrationTest {
	@Test
	public void defaultStorageUsesGeneralizedSuffixTree() {
		ISearchStorage<String> searchStorage = PluginLoader.createSearchStorageFactory(List.of()).createSearchStorage();

		Assertions.assertInstanceOf(GeneralizedSuffixTree.class, searchStorage);
	}

	@Test
	public void customFactoryIsUsedByRegistration() {
		List<RecordingSearchStorage<?>> createdStorages = new ArrayList<>();
		AdvancedSearchRegistration searchRegistration = new AdvancedSearchRegistration();
		searchRegistration.replaceSearchStorage(new ISearchStorageFactory() {
			@Override
			public <T> ISearchStorage<T> createSearchStorage() {
				RecordingSearchStorage<T> storage = new RecordingSearchStorage<>();
				createdStorages.add(storage);
				return storage;
			}
		});

		ISearchStorageFactory searchStorageFactory = searchRegistration.getSearchStorageFactoryOverride()
			.orElseThrow();
		ISearchStorage<String> searchStorage = searchStorageFactory.createSearchStorage();

		Assertions.assertEquals(1, createdStorages.size());
		Assertions.assertSame(createdStorages.get(0), searchStorage);
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
