package mezz.jei.core.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CombinedSearchables<T> implements ISearchable<T> {
	private final List<ISearchable<T>> searchables = new ArrayList<>();

	@Override
	public void getSearchResults(String word, Consumer<Collection<T>> resultsConsumer) {
		for (ISearchable<T> searchable : this.searchables) {
			if (searchable.getMode() == SearchMode.ENABLED) {
				searchable.getSearchResults(word, resultsConsumer);
			}
		}
	}

	@Override
	public void getAllElements(Consumer<Collection<T>> resultsConsumer) {
		for (ISearchable<T> searchable : this.searchables) {
			if (searchable.getMode() == SearchMode.ENABLED) {
				searchable.getAllElements(resultsConsumer);
			}
		}
	}

	public void addSearchable(ISearchable<T> searchable) {
		this.searchables.add(searchable);
	}
}
