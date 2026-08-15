package mezz.jei.api.runtime.config;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Serialization and validation helper for JEI config values.
 *
 * @since 10.5.0
 */
@ApiStatus.NonExtendable
public interface IJeiConfigListValueSerializer<T> extends IJeiConfigValueSerializer<List<T>> {
	/**
	 * Get the serializer for each value in the list.
	 *
	 * @since 10.5.0
	 */
	IJeiConfigValueSerializer<T> getListValueSerializer();
}
