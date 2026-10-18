package mezz.jei.api.search;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Storage backend used by JEI's ingredient search.
 *
 * @param <T> the type of values stored in the search index
 * @since 29.16.0
 */
public interface ISearchStorage<T> {
	/**
	 * Search for values matching the token.
	 *
	 * @param token the token to search for
	 * @param resultsConsumer accepts collections of matching results
	 * @since 29.16.0
	 */
	void getSearchResults(String token, Consumer<Collection<T>> resultsConsumer);

	/**
	 * Get all values stored in this search index.
	 *
	 * @since 29.16.0
	 */
	void getAllElements(Consumer<Collection<T>> resultsConsumer);

	/**
	 * Add a value to the search index.
	 *
	 * @param key the searchable string
	 * @param value the indexed value
	 * @since 29.16.0
	 */
	void put(String key, T value);

	/**
	 * Whether {@link #removeAll(Collection)} is supported.
	 * This capability must remain constant for the lifetime of the storage.
	 *
	 * @since 29.52.0
	 */
	default boolean supportsDeletion() {
		return false;
	}

	/**
	 * Remove the given value instances and all their search entries, comparing values by identity ({@code ==}).
	 *
	 * If a rebuild is required, return {@code false} without changes.
	 *
	 * @return {@code true} if removal completed, even when no values matched; {@code false} if a rebuild is required
	 * @throws UnsupportedOperationException if {@link #supportsDeletion()} is {@code false}
	 * @since 29.52.0
	 */
	default boolean removeAll(Collection<T> values) {
		throw new UnsupportedOperationException("Search storage does not support deletion");
	}

	/**
	 * Get implementation-specific statistics for logging.
	 *
	 * @since 29.16.0
	 */
	String statistics();
}
