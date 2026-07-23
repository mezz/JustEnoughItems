package mezz.jei.api.search;

/**
 * Builder for a search storage.
 *
 * @param <T> the type of values stored in the search index
 * @since 15.23.0
 */
public interface ISearchStorageBuilder<T> {
	/**
	 * Add a value to the search index builder.
	 *
	 * @param key the searchable string
	 * @param value the indexed value
	 * @since 15.23.0
	 */
	void put(String key, T value);

	/**
	 * Build the search storage.
	 *
	 * @since 15.23.0
	 */
	ISearchStorage<T> build();
}
