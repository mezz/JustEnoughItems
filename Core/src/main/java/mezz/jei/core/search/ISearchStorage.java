package mezz.jei.core.search;

import java.util.Collection;
import java.util.function.Consumer;

public interface ISearchStorage<T> {
	void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer);

	void getAllElements(Consumer<Collection<T>> resultsConsumer);

	void put(String key, T value);

	String statistics();
}
