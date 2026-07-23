package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;

public class SearchStorageBuilderAdapter<T> implements ISearchStorageBuilder<T> {
	private final ISearchStorage<T> searchStorage;

	public SearchStorageBuilderAdapter(ISearchStorage<T> searchStorage) {
		this.searchStorage = searchStorage;
	}

	@Override
	public void put(String key, T value) {
		searchStorage.put(key, value);
	}

	@Override
	public ISearchStorage<T> build() {
		return searchStorage;
	}
}
