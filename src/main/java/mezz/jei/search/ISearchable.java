package mezz.jei.search;

import mezz.jei.config.SearchMode;

import java.util.Set;

public interface ISearchable<T> {
	void getSearchResults(String token, Set<T> results);

	void getAllElements(Set<T> results);

	default SearchMode getMode() {
		return SearchMode.ENABLED;
	}
}
