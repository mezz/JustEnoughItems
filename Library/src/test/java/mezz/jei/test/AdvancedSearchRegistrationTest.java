package mezz.jei.test;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.core.search.BakedSubstringIndexBuilder;
import mezz.jei.core.search.LimitedStringStorageBuilder;
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
	public void defaultStorageUsesBakedSubstringIndexBuilder() {
		ISearchStorageBuilder<String> searchStorageBuilder = PluginLoader.createSearchStorageFactory(new ArrayList<>()).create();

		Assertions.assertInstanceOf(BakedSubstringIndexBuilder.class, searchStorageBuilder);
	}

	@Test
	public void defaultStorageFactoryIsAvailableFromRegistration() {
		AdvancedSearchRegistration searchRegistration = new AdvancedSearchRegistration();
		ISearchStorageBuilder<String> searchStorageBuilder = searchRegistration.getDefaultSearchStorageBuilderFactory().create();

		Assertions.assertInstanceOf(BakedSubstringIndexBuilder.class, searchStorageBuilder);
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

		ISearchStorageBuilderFactory searchStorageBuilderFactory = searchRegistration.getSearchStorageBuilderFactoryOverride()
			.orElseThrow();
		ISearchStorage<String> searchStorage = searchStorageBuilderFactory.<String>create().build();

		Assertions.assertEquals(1, createdStorages.size());
		Assertions.assertSame(createdStorages.get(0), searchStorage);
	}

	@Test
	public void builderFactoryCreateWithIdPassesIdToFactory() {
		RecordingSearchStorageBuilderFactory searchStorageBuilderFactory = new RecordingSearchStorageBuilderFactory();
		String id = createSearchStorageId();

		searchStorageBuilderFactory.create(id);

		Assertions.assertEquals(1, searchStorageBuilderFactory.createdBuilders.size());
		Assertions.assertSame(id, searchStorageBuilderFactory.ids.get(0));
	}

	@Test
	public void limitedStringStorageBuilderForwardsSearchStorageId() {
		RecordingSearchStorageBuilderFactory searchStorageBuilderFactory = new RecordingSearchStorageBuilderFactory();
		String id = createSearchStorageId();

		new LimitedStringStorageBuilder<>(searchStorageBuilderFactory, id);

		Assertions.assertEquals(1, searchStorageBuilderFactory.createdBuilders.size());
		Assertions.assertSame(id, searchStorageBuilderFactory.ids.get(0));
	}

	private static String createSearchStorageId() {
		return "test";
	}

	private static class RecordingSearchStorageBuilderFactory implements ISearchStorageBuilderFactory {
		private final List<RecordingSearchStorageBuilder<?>> createdBuilders = new ArrayList<>();
		private final List<String> ids = new ArrayList<>();

		@Override
		public <T> ISearchStorageBuilder<T> create() {
			RecordingSearchStorageBuilder<T> builder = new RecordingSearchStorageBuilder<>();
			createdBuilders.add(builder);
			return builder;
		}

		@Override
		public <T> ISearchStorageBuilder<T> create(String id) {
			ids.add(id);
			return create();
		}
	}

	private static class RecordingSearchStorageBuilder<T> implements ISearchStorageBuilder<T> {
		@Override
		public void put(String key, T value) {

		}

		@Override
		public ISearchStorage<T> build() {
			return new RecordingSearchStorage<>();
		}
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
