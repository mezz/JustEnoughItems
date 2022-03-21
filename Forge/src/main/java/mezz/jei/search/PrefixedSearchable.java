package mezz.jei.search;

import mezz.jei.config.SearchMode;
import mezz.jei.ingredients.IListElementInfo;

import java.util.Collection;
import java.util.Set;

public class PrefixedSearchable implements ISearchable<IListElementInfo<?>> {
	private final ISearchStorage<IListElementInfo<?>> searchStorage;
	private final PrefixInfo prefixInfo;

	public PrefixedSearchable(ISearchStorage<IListElementInfo<?>> searchStorage, PrefixInfo prefixInfo) {
		this.searchStorage = searchStorage;
		this.prefixInfo = prefixInfo;
	}

	public ISearchStorage<IListElementInfo<?>> getSearchStorage() {
		return searchStorage;
	}

	public Collection<String> getStrings(IListElementInfo<?> element) {
		return prefixInfo.getStrings(element);
	}

	@Override
	public SearchMode getMode() {
		return prefixInfo.getMode();
	}

	@Override
	public void getSearchResults(String token, Set<IListElementInfo<?>> results) {
		searchStorage.getSearchResults(token, results);
	}

	@Override
	public void getAllElements(Set<IListElementInfo<?>> results) {
		searchStorage.getAllElements(results);
	}
}
