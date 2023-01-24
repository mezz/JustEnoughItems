package mezz.jei.api.runtime.config;

import java.util.List;

/**
 * Serialization and validation helper for JEI config values.
 *
 * @since 11.7.1
 */
public interface IJeiConfigListValueSerializer<T> extends IJeiConfigValueSerializer<List<T>> {
    /**
     * Get the serializer for each value in the list.
     *
     * @since 11.7.1
     */
    IJeiConfigValueSerializer<T> getListValueSerializer();
}
