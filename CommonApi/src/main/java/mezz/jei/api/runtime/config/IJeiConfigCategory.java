package mezz.jei.api.runtime.config;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Categories organize {@link IJeiConfigValue}s into groups.
 * An {@link IJeiConfigFile} can contain one or more categories.
 *
 * @since 12.1.0
 */
public interface IJeiConfigCategory {
	/**
	 * The name of the category.
	 *
	 * @since 12.1.0
	 */
	String getName();

	/**
	 * The config values in the category.
	 *
	 * @since 12.1.0
	 */
	@Unmodifiable
	Collection<? extends IJeiConfigValue<?>> getConfigValues();

	/**
	 * Get a specific Config Value from this category.
	 *
	 * @since ?.?.?
	 */
	@Unmodifiable
	Optional<? extends IJeiConfigValue<?>> getConfigValue(String configValueName);

	/**
	 * Get a list of Config Value names in this category.
	 *
	 * @since ?.?.?
	 */
	Set<String> getValueNames();
}
