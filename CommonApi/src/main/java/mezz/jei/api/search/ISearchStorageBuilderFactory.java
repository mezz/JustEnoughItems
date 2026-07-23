package mezz.jei.api.search;

/**
 * Creates search storage builders for JEI's ingredient search.
 *
 * <p>
 * JEI creates a separate builder for each independent search index. Each builder receives the initial indexed values
 * before {@link ISearchStorageBuilder#build()} is called.
 * </p>
 *
 * @since 15.23.0
 */
@FunctionalInterface
public interface ISearchStorageBuilderFactory {
	/**
	 * Create a new empty search storage builder.
	 *
	 * @param <T> the type of values stored in the search index
	 * @since 15.23.0
	 */
	<T> ISearchStorageBuilder<T> create();
}
