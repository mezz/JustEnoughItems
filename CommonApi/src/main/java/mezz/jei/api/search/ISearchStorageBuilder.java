package mezz.jei.api.search;

/**
 * Builder for a search storage backend used by JEI's ingredient search.
 *
 * <p>
 * JEI adds the initial ingredient data to a builder before calling {@link #build()}.
 * This allows implementations to preprocess or bake the initial search index before JEI starts searching.
 * The returned {@link ISearchStorage} is used for searches and for ingredients added at runtime.
 * </p>
 *
 * @param <T> the type of values stored in the search index
 * @since 19.41.0
 */
public interface ISearchStorageBuilder<T> {
	/**
	 * Add a value to the initial search index.
	 *
	 * @param key the searchable string
	 * @param value the indexed value
	 * @since 19.41.0
	 */
	void put(String key, T value);

	/**
	 * Create the final search storage from the values added to this builder.
	 *
	 * <p>
	 * The returned storage must support {@link ISearchStorage#put(String, Object)} for ingredients added after JEI's
	 * initial startup search index has been built.
	 * </p>
	 *
	 * @since 19.41.0
	 */
	ISearchStorage<T> build();
}
