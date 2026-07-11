package mezz.jei.library.load.registration;

import mezz.jei.api.registration.IAdvancedSearchRegistration;
import mezz.jei.api.search.ISearchStorage;
import mezz.jei.common.search.GeneralizedSuffixTreeSearchStorage;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class AdvancedSearchRegistration implements IAdvancedSearchRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private Supplier<? extends ISearchStorage<?>> searchStorageSupplier = GeneralizedSuffixTreeSearchStorage::new;

	@Override
	public void replaceSearchStorage(Supplier<? extends ISearchStorage<?>> searchStorageSupplier) {
		ErrorUtil.checkNotNull(searchStorageSupplier, "searchStorageSupplier");

		LOGGER.info("Replaced search storage supplier: {}", searchStorageSupplier);
		this.searchStorageSupplier = searchStorageSupplier;
	}

	public Supplier<? extends ISearchStorage<?>> getSearchStorageSupplier() {
		return searchStorageSupplier;
	}
}
