package mezz.jei.core.search;

import java.util.Collection;
import java.util.function.Consumer;

public class PrefixedSearchable<T, I> implements ISearchable<I> {
	private final ISearchStorage<I> searchStorage;
	private final PrefixInfo<T, I> prefixInfo;

	public PrefixedSearchable(ISearchStorage<I> searchStorage, PrefixInfo<T, I> prefixInfo) {
		this.searchStorage = searchStorage;
		this.prefixInfo = prefixInfo;
	}

	public ISearchStorage<I> getSearchStorage() {
		return searchStorage;
	}

	public Collection<String> getStrings(T element) {
		return prefixInfo.getStrings(element);
	}

	@Override
	public SearchMode getMode() {
		return prefixInfo.getMode();
	}

	@Override
	public void getSearchResults(String token, Consumer<Collection<I>> resultsConsumer) {
		searchStorage.getSearchResults(token, resultsConsumer);
	}

	@Override
	public void getAllElements(Consumer<Collection<I>> resultsConsumer) {
		searchStorage.getAllElements(resultsConsumer);
	}
}
