package mezz.jei.common.search;

import mezz.jei.api.search.ISearchStorage;
import net.mezzdev.bakedsubstring.BakedSubstringIndex;

import java.util.Collection;
import java.util.function.Consumer;

public class BakedSubstringIndexSearchStorage<T> implements ISearchStorage<T> {
	private final BakedSubstringIndex<T> bakedStorage;
	private final GeneralizedSuffixTreeSearchStorage<T> mutableStorage = new GeneralizedSuffixTreeSearchStorage<>();

	public BakedSubstringIndexSearchStorage(BakedSubstringIndex<T> bakedStorage) {
		this.bakedStorage = bakedStorage;
	}

	@Override
	public void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer) {
		resultsConsumer.accept(bakedStorage.getSearchResults(token));
		mutableStorage.getSearchResults(token, resultsConsumer);
	}

	@Override
	public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		resultsConsumer.accept(bakedStorage.getAllElements());
		mutableStorage.getAllElements(resultsConsumer);
	}

	@Override
	public void put(String key, T value) {
		mutableStorage.put(key, value);
	}

	@Override
	public String statistics() {
		return "BakedSubstringIndexSearchStorage: " +
			"baked=" + bakedStorage +
			", runtimeStorage=" + mutableStorage.statistics();
	}
}
