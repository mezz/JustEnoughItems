package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.search.ILanguageTransformer;

/**
 * The {@link ISearchRegistration} instance is passed to your mod plugin in {@link IModPlugin#registerSearch(ISearchRegistration)}.
 *
 * @since 12.2.0
 */
public interface ISearchRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 *
	 * @since 12.2.0
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Register your own {@link ILanguageTransformer} here.
	 *
	 * @since 12.2.0
	 */
	void addLanguageTransformer(ILanguageTransformer languageTransformer);
}
