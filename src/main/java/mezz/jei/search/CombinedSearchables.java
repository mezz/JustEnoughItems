package mezz.jei.search;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.config.SearchMode;

public class CombinedSearchables implements ISearchable {
	private final List<ISearchable> searchables = new ArrayList<>();

	@Override
	public void addSearchResults(String word, IntSet results) {
		for (ISearchable searchable : this.searchables) {
			if (searchable.getMode() == SearchMode.ENABLED) {
				searchable.addSearchResults(word, results);
			}
		}
	}

	public void addSearchable(ISearchable searchable) {
		this.searchables.add(searchable);
	}
}
