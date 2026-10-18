package mezz.jei.test;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IAdvancedSearchRegistration;
import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.common.search.LuceneSearchStorageBuilder;
import mezz.jei.common.search.LuceneSearchStorage;
import mezz.jei.common.search.LimitedStringStorageBuilder;
import mezz.jei.library.load.PluginLoader;
import mezz.jei.library.load.registration.AdvancedSearchRegistration;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class AdvancedSearchRegistrationTest {
	@Test
	public void defaultStorageUsesLuceneSearchStorageBuilder() {
		ISearchStorageBuilder<String> searchStorageBuilder = PluginLoader.createSearchStorageFactory(new ArrayList<>()).create();

		Assertions.assertInstanceOf(LuceneSearchStorageBuilder.class, searchStorageBuilder);
	}

	@Test
	public void defaultStorageFactoryIsAvailableFromRegistration() {
		AdvancedSearchRegistration searchRegistration = new AdvancedSearchRegistration();
		ISearchStorageBuilder<String> searchStorageBuilder = searchRegistration.getDefaultSearchStorageBuilderFactory().create();

		Assertions.assertInstanceOf(LuceneSearchStorageBuilder.class, searchStorageBuilder);
	}

	@Test
	public void defaultFactoryCanBeRegisteredWithoutWrapping() {
		IModPlugin plugin = new IModPlugin() {
			@Override
			public ResourceLocation getPluginUid() {
				return new ResourceLocation("jei_test", "default_search_storage");
			}

			@Override
			public void registerAdvancedSearch(IAdvancedSearchRegistration registration) {
				registration.replaceSearchStorage(registration.getDefaultSearchStorageBuilderFactory());
			}
		};
		ISearchStorageBuilder<String> builder = PluginLoader.createSearchStorageFactory(List.of(plugin)).create("names");
		Assertions.assertInstanceOf(LuceneSearchStorageBuilder.class, builder);
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

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void usesNativeDeletionForBothRegistrationOverloads(boolean useBuilder) {
		List<ISearchStorage<?>> registeredStorages = new ArrayList<>();
		IModPlugin plugin = new IModPlugin() {
			@Override
			public ResourceLocation getPluginUid() {
				return new ResourceLocation("jei_test", "native_deletion");
			}

			@Override
			public void registerAdvancedSearch(IAdvancedSearchRegistration registration) {
				if (useBuilder) {
					registration.replaceSearchStorage(new ISearchStorageBuilderFactory() {
						@Override
						public <T> ISearchStorageBuilder<T> create() {
							return new LuceneSearchStorageBuilder<>() {
								@Override
								public ISearchStorage<T> build() {
									ISearchStorage<T> storage = super.build();
									registeredStorages.add(storage);
									return storage;
								}
							};
						}

						@Override
						public boolean supportsDeletion() {
							return true;
						}
					});
				} else {
					registration.replaceSearchStorage(new ISearchStorageFactory() {
						@Override
						public <T> ISearchStorage<T> createSearchStorage() {
							LuceneSearchStorage<T> storage = new LuceneSearchStorage<>();
							registeredStorages.add(storage);
							return storage;
						}
					});
				}
			}
		};
		ISearchStorageBuilder<String> builder = PluginLoader.createSearchStorageFactory(List.of(plugin)).create("names");
		builder.put("old changed", "changed");
		builder.put("old unchanged", "unchanged");
		ISearchStorage<String> storage = builder.build();
		Assertions.assertEquals(1, registeredStorages.size());
		Assertions.assertSame(registeredStorages.get(0), storage);

		Assertions.assertTrue(storage.removeAll(List.of("changed")));
		registeredStorages.get(0).getSearchResults("old changed", matches -> Assertions.assertTrue(matches.isEmpty()));
		storage.put("new changed", "changed");
		Set<String> results = new HashSet<>();
		storage.getSearchResults("old", results::addAll);
		Assertions.assertEquals(Set.of("unchanged"), results);
		results.clear();
		storage.getSearchResults("new", results::addAll);
		Assertions.assertEquals(Set.of("changed"), results);
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
