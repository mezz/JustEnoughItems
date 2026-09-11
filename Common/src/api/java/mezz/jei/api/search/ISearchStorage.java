package mezz.jei.api.search;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Storage backend used by JEI's ingredient search.
 *
 * @param <T> the type of values stored in the search index
 * @since 19.37.0
 */
public interface ISearchStorage<T> {
	/**
	 * Search for values matching the token.
	 *
	 * @param token the token to search for
	 * @param resultsConsumer accepts collections of matching results
	 * @since 19.37.0
	 */
	void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer);

	/**
	 * Get all values stored in this search index.
	 *
	 * @since 19.37.0
	 */
	void getAllElements(Consumer<Collection<T>> resultsConsumer);

	/**
	 * Add a value to the search index.
	 *
	 * @param key the searchable string
	 * @param value the indexed value
	 * @since 19.37.0
	 */
	void put(String key, T value);

	/**
	 * Get implementation-specific statistics for logging.
	 *
	 * @since 19.37.0
	 */
	String statistics();
}
