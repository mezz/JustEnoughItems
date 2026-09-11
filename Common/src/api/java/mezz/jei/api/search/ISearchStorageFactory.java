package mezz.jei.api.search;

/**
 * Creates search storage instances for JEI's ingredient search.
 *
 * @since 30.10.0
 */
@FunctionalInterface
public interface ISearchStorageFactory {
	/**
	 * Create a new empty search storage.
	 *
	 * @param <T> the type of values stored in the search index
	 * @since 30.10.0
	 */
	<T> ISearchStorage<T> createSearchStorage();
}
