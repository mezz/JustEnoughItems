package mezz.jei.api.runtime.config;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Serialization and validation helper for JEI config values.
 *
 * @since 11.7.0
 * @deprecated MezzConfig list values now use {@code net.mezzdev.config.api.value.serializer.IConfigValueSerializer<java.util.List<T>>}
 */
@Deprecated(since = "11.66.0", forRemoval = true)
@ApiStatus.NonExtendable
@SuppressWarnings({"removal", "DeprecatedIsStillUsed"})
public interface IJeiConfigListValueSerializer<T> extends IJeiConfigValueSerializer<List<T>> {
	/**
	 * Get the serializer for each value in the list.
	 *
	 * @since 11.7.0
	 */
	IJeiConfigValueSerializer<T> getListValueSerializer();
}
