package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.search.ISearchStorageFactory;

/**
 * The IAdvancedSearchRegistration instance is passed to your mod plugin in {@link IModPlugin#registerAdvancedSearch(IAdvancedSearchRegistration)}.
 *
 * @since 29.16.0
 */
public interface IAdvancedSearchRegistration {
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
	 * @since 29.16.0
	 */
	void replaceSearchStorage(ISearchStorageFactory searchStorageFactory);
}
