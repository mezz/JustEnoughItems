package mezz.jei.core.search;

import java.util.Set;

public interface ISearchStorage<T> {
	void getSearchResults(String token, Set<T> results);

	void getAllElements(Set<T> results);

	void put(String key, T value);

	String statistics();
}
