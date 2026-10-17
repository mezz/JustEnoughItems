package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;

/**
 * Builds a {@link LuceneSearchStorage} from the initial ingredient data.
 * <p>
 * Single-use: calls to {@link #put(String, Object)} and {@link #build()} fail after building.
 */
public class LuceneSearchStorageBuilder<T> implements ISearchStorageBuilder<T> {
	private final LuceneSearchStorage<T> storage = new LuceneSearchStorage<>();
	private boolean built;

	@Override
	public void put(String key, T value) {
		checkNotBuilt();
		storage.put(key, value);
	}

	@Override
	public ISearchStorage<T> build() {
		checkNotBuilt();
		built = true;
		storage.refresh();
		return storage;
	}

	private void checkNotBuilt() {
		if (built) {
			throw new IllegalStateException("Search storage has already been built");
		}
	}
}
