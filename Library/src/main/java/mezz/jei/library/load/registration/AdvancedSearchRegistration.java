package mezz.jei.library.load.registration;

import mezz.jei.api.registration.IAdvancedSearchRegistration;
import mezz.jei.api.search.ISearchStorageFactory;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancedSearchRegistration implements IAdvancedSearchRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	private ISearchStorageFactory searchStorageFactoryOverride;

	@Override
	public void replaceSearchStorage(ISearchStorageFactory searchStorageFactory) {
		ErrorUtil.checkNotNull(searchStorageFactory, "searchStorageFactory");

		LOGGER.info("Replaced search storage factory: {}", searchStorageFactory);
		this.searchStorageFactoryOverride = searchStorageFactory;
	}

	public Optional<ISearchStorageFactory> getSearchStorageFactoryOverride() {
		return Optional.ofNullable(searchStorageFactoryOverride);
	}
}
