package mezz.jei.api.runtime.config;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Serialization and validation helper for JEI config values.
 *
 * @since 12.1.1
 */
@ApiStatus.NonExtendable
public interface IJeiConfigListValueSerializer<T> extends IJeiConfigValueSerializer<List<T>> {
	/**
	 * Get the serializer for each value in the list.
	 *
	 * @since 12.1.1
	 */
	IJeiConfigValueSerializer<T> getListValueSerializer();
}
