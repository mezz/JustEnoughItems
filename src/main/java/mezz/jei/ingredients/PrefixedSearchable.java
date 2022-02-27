package mezz.jei.ingredients;

import java.util.Collection;
import java.util.Set;

import mezz.jei.config.SearchMode;
import mezz.jei.search.ISearchable;
import mezz.jei.search.PrefixInfo;
import mezz.jei.search.suffixtree.GeneralizedSuffixTree;

public class PrefixedSearchable implements ISearchable<IListElementInfo<?>> {
	private final GeneralizedSuffixTree<IListElementInfo<?>> searchable;
	private final PrefixInfo prefixInfo;

	public PrefixedSearchable(GeneralizedSuffixTree<IListElementInfo<?>> searchable, PrefixInfo prefixInfo) {
		this.searchable = searchable;
		this.prefixInfo = prefixInfo;
	}

	public GeneralizedSuffixTree<IListElementInfo<?>> getSearchable() {
		return searchable;
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
		searchable.getSearchResults(token, results);
	}

	@Override
	public void getAllElements(Set<IListElementInfo<?>> results) {
		searchable.getAllElements(results);
	}
}
