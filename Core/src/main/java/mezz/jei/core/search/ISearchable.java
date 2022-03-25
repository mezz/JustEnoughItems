package mezz.jei.core.search;

import java.util.Set;

public interface ISearchable<T> {
	void getSearchResults(String token, Set<T> results);

	void getAllElements(Set<T> results);

	default SearchMode getMode() {
		return SearchMode.ENABLED;
	}
}
