package mezz.jei.api.registration;

import java.util.Arrays;
import java.util.Collection;

/**
 * Registers additional information that helps JEI identify a mod.
 *
 * @since 7.16.0
 */
public interface IModInfoRegistration {
	/**
	 * Register alternative mod names used for searching.
	 *
	 * @since 7.16.0
	 */
	void addModAliases(String modId, Collection<String> aliases);

	/**
	 * Register alternative mod names used for searching.
	 *
	 * @since 7.16.0
	 */
	default void addModAliases(String modId, String... aliases) {
		addModAliases(modId, Arrays.asList(aliases));
	}
}
