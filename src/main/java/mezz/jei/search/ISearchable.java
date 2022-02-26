package mezz.jei.search;

import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.config.SearchMode;

public interface ISearchable {
	void addSearchResults(String token, IntSet results);

	default SearchMode getMode() {
		return SearchMode.ENABLED;
	}
}
