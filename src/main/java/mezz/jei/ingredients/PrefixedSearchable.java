package mezz.jei.ingredients;

import java.util.Collection;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.config.SearchMode;
import mezz.jei.search.ISearchable;
import mezz.jei.search.PrefixInfo;

public class PrefixedSearchable<T extends ISearchable> implements ISearchable {
	private final T searchable;
	private final PrefixInfo prefixInfo;

	public PrefixedSearchable(T searchable, PrefixInfo prefixInfo) {
		this.searchable = searchable;
		this.prefixInfo = prefixInfo;
	}

	public T getSearchable() {
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
	public IntSet search(String word) {
		return searchable.search(word);
	}

}
