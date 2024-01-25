package mezz.jei.api.registration;

import java.util.Collection;
import java.util.Set;

/**
 * Register additional mod info to help JEI understand your mod better.
 *
 * @since 17.1.0
 */
public interface IModInfoRegistration {
    /**
     * Register alternative mod names, used for searching for a mod by a different name.
     *
     * For example "Just Enough Items" can register an alias "JEI" to help make searching for it easier.
     *
     * @param modId The modId to register aliases for
     * @param aliases The aliases to register
     * @since 17.1.0
     */
    void addModAliases(String modId, Collection<String> aliases);

    /**
     * Register alternative mod names, used for searching for a mod by a different name.
     *
     * For example "Just Enough Items" can register an alias "JEI" to help make searching for it easier.
     *
     * @param modId The modId to register aliases for
     * @param aliases The aliases to register
     * @since 17.1.0
     */
    default void addModAliases(String modId, String... aliases) {
        addModAliases(modId, Set.of(aliases));
    }
}
