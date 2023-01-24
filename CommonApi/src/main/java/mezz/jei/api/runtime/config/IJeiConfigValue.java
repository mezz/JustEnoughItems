package mezz.jei.api.runtime.config;

/**
 * Represents config value used by JEI.
 * Config values can be read or updated by mods that display in-game config files to players.
 *
 * These config values are automatically synced with the config file.
 * {@link #getValue()} will automatically update based on changes to the file,
 * and using {@link #set} will automatically update the file.
 *
 * @since 12.1.0
 */
public interface IJeiConfigValue<T> {
    /**
     * Get the name of this config value.
     *
     * @since 12.1.0
     */
    String getName();

    /**
     * Get the description of this config value.
     *
     * @since 12.1.0
     */
    String getDescription();

    /**
     * Get the current value.
     * This will automatically update and load from the config file if there are changes.
     *
     * @since 12.1.0
     */
    T getValue();

    /**
     * Get the default value.
     *
     * @since 12.1.0
     */
    T getDefaultValue();

    /**
     * Set the config value to the given value.
     * This will automatically mark the config file as dirty so that it will save the new values.
     *
     * @since 12.1.0
     */
    boolean set(T value);

    /**
     * Get the helper for serializing values to and from Strings, and validating values.
     *
     * @since 12.1.1
     */
    IJeiConfigValueSerializer<T> getSerializer();
}
