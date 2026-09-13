package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.search.ISearchStorageBuilder;
import mezz.jei.api.search.ISearchStorageBuilderFactory;
import mezz.jei.api.search.ISearchStorageFactory;
import org.jetbrains.annotations.ApiStatus;

/**
 * The IAdvancedSearchRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvancedSearch(IAdvancedSearchRegistration)}.
 *
 * @since 30.10.0
 */
@ApiStatus.NonExtendable
public interface IAdvancedSearchRegistration {
	/**
	 * Get JEI's default search storage builder factory.
	 *
	 * <p>
	 * This allows plugins to wrap JEI's default search storage while preserving JEI's normal indexing and matching
	 * behavior.
	 * </p>
	 *
	 * @since 30.15.0
	 */
	ISearchStorageBuilderFactory getDefaultSearchStorageBuilderFactory();

	/**
	 * Replace JEI's default ingredient search storage with a custom implementation.
	 *
	 * <p>
	 * This completely replaces JEI's default search storage, and is an advanced hook for implementations that need
	 * different indexing or matching behavior than JEI's default substring search. For example, a custom storage can
	 * support language-aware matching where users type phonetic, transliterated, or otherwise normalized search terms
	 * that should match localized ingredient names.
	 * </p>
	 *
	 * <p>
	 * The factory must create a new empty storage instance each time it is called. JEI uses this replacement for all
	 * indexed ingredient search storage, including the backing indexes inside limited string storage.
	 * If multiple plugins replace the search storage, the last replacement is used.
	 * </p>
	 *
	 * <p>
	 * This overload creates live search storage directly. The storage receives both initial ingredient data and runtime
	 * additions through {@link mezz.jei.api.search.ISearchStorage#put}, with no separate build step after the initial
	 * data has been added.
	 * </p>
	 *
	 * <p>
	 * For implementations that need to preprocess or bake the initial search index before JEI starts using it, use
	 * {@link #replaceSearchStorage(ISearchStorageBuilderFactory)}.
	 * </p>
	 *
	 * @since 30.10.0
	 */
	void replaceSearchStorage(ISearchStorageFactory searchStorageFactory);

	/**
	 * Replace JEI's default ingredient search storage with a custom builder implementation.
	 *
	 * <p>
	 * This completely replaces JEI's default search storage, and is an advanced hook for implementations that need
	 * different indexing or matching behavior than JEI's default substring search. For example, a custom storage can
	 * support language-aware matching where users type phonetic, transliterated, or otherwise normalized search terms
	 * that should match localized ingredient names.
	 * </p>
	 *
	 * <p>
	 * The factory must create a new empty builder each time it is called. JEI uses this replacement for all indexed
	 * ingredient search storage, including the backing indexes inside limited string storage.
	 * Builders are single-use: JEI adds the initial data, calls {@link ISearchStorageBuilder#build()}, and then uses
	 * the returned storage.
	 * If multiple plugins replace the search storage, the last replacement is used.
	 * </p>
	 *
	 * <p>
	 * This differs from {@link #replaceSearchStorage(ISearchStorageFactory)}, which creates live storage directly and
	 * has no build phase. Use this builder overload when the search implementation can preprocess or bake the initial
	 * ingredient index for faster searching. The built storage is still used for runtime additions after startup.
	 * </p>
	 *
	 * @since 30.13.0
	 */
	void replaceSearchStorage(ISearchStorageBuilderFactory searchStorageBuilderFactory);
}
