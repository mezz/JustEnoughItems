package mezz.jei.core.search;

import mezz.jei.api.config.SearchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CombinedSearchables<T> implements ISearchable<T> {
	private final List<ISearchable<T>> searchables = new ArrayList<>();

	@Override
	public void getSearchResults(String word, Set<T> results) {
		for (ISearchable<T> searchable : this.searchables) {
			if (searchable.getMode() == SearchMode.ENABLED) {
				searchable.getSearchResults(word, results);
			}
		}
	}

	@Override
	public void getAllElements(Set<T> results) {
		for (ISearchable<T> searchable : this.searchables) {
			if (searchable.getMode() == SearchMode.ENABLED) {
				searchable.getAllElements(results);
			}
		}
	}

	public void addSearchable(ISearchable<T> searchable) {
		this.searchables.add(searchable);
	}
}
