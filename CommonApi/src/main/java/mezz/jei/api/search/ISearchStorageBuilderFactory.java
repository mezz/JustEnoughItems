package mezz.jei.api.search;

import java.util.Objects;

/**
 * Creates search storage builders for JEI's ingredient search.
 *
 * <p>
 * JEI creates a separate builder for each independent search index. Each builder receives the initial indexed values
 * before {@link ISearchStorageBuilder#build()} is called.
 * </p>
 *
 * @since 30.13.0
 */
@FunctionalInterface
public interface ISearchStorageBuilderFactory {
	/**
	 * Create a new empty search storage builder.
	 *
	 * @param <T> the type of values stored in the search index
	 * @since 30.13.0
	 */
	<T> ISearchStorageBuilder<T> create();

	/**
	 * Create a new empty search storage builder with a stable id for the logical search index.
	 *
	 * <p>
	 * JEI calls this overload when it can provide a stable id to help implementations distinguish between different
	 * search storages in debug output, benchmark output, or dumps. Example ids include {@code unprefixed},
	 * {@code tooltips}, and {@code tags}.
	 * </p>
	 *
	 * @param id stable id for the logical search index
	 * @param <T> the type of values stored in the search index
	 * @since 30.15.0
	 */
	default <T> ISearchStorageBuilder<T> create(String id) {
		Objects.requireNonNull(id, "id");
		return create();
	}
}
