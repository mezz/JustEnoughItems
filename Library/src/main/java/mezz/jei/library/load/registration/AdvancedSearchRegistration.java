package mezz.jei.library.load.registration;

import mezz.jei.api.registration.IAdvancedSearchRegistration;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.common.search.BakedSubstringIndexBuilder;
import mezz.jei.common.search.SearchStorageBuilderAdapter;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancedSearchRegistration implements IAdvancedSearchRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	private ISearchStorageBuilderFactory searchStorageBuilderFactoryOverride;
	private final ISearchStorageBuilderFactory defaultSearchStorageBuilderFactory;

	public AdvancedSearchRegistration() {
		this(BakedSubstringIndexBuilder::new);
	}

	public AdvancedSearchRegistration(ISearchStorageBuilderFactory defaultSearchStorageBuilderFactory) {
		ErrorUtil.checkNotNull(defaultSearchStorageBuilderFactory, "defaultSearchStorageBuilderFactory");
		this.defaultSearchStorageBuilderFactory = defaultSearchStorageBuilderFactory;
	}

	@Override
	public ISearchStorageBuilderFactory getDefaultSearchStorageBuilderFactory() {
		return defaultSearchStorageBuilderFactory;
	}

	@Override
	public void replaceSearchStorage(ISearchStorageFactory searchStorageFactory) {
		ErrorUtil.checkNotNull(searchStorageFactory, "searchStorageFactory");

		LOGGER.info("Replaced search storage factory: {}", searchStorageFactory);
		this.searchStorageBuilderFactoryOverride = new ISearchStorageBuilderFactory() {
			@Override
			public <T> ISearchStorageBuilder<T> create() {
				return new SearchStorageBuilderAdapter<>(searchStorageFactory.createSearchStorage());
			}
		};
	}

	@Override
	public void replaceSearchStorage(ISearchStorageBuilderFactory searchStorageBuilderFactory) {
		ErrorUtil.checkNotNull(searchStorageBuilderFactory, "searchStorageBuilderFactory");

		LOGGER.info("Replaced search storage factory: {}", searchStorageBuilderFactory);
		this.searchStorageBuilderFactoryOverride = searchStorageBuilderFactory;
	}

	public Optional<ISearchStorageBuilderFactory> getSearchStorageBuilderFactoryOverride() {
		return Optional.ofNullable(searchStorageBuilderFactoryOverride);
	}
}
