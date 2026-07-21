package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import mezz.jei.api.search.ISearchStorageBuilder;
import net.mezzdev.bakedsubstring.BakedSubstringIndex;

public class BakedSubstringIndexBuilder<T> implements ISearchStorageBuilder<T> {
	private final BakedSubstringIndex.Builder<T> builder = BakedSubstringIndex.builder();

	@Override
	public void put(String key, T value) {
		builder.put(key, value);
	}

	@Override
	public ISearchStorage<T> build() {
		BakedSubstringIndex<T> bakedStorage = builder.build();
		return new BakedSubstringIndexSearchStorage<>(bakedStorage);
	}
}
