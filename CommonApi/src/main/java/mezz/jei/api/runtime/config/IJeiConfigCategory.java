package mezz.jei.api.runtime.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

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

	Component getLocalizedName();

	Component getDescription();

	/**
	 * The config values in the category.
	 *
	 * @since 12.1.0
	 */
	@Unmodifiable
	Collection<? extends IJeiConfigValue<?>> getConfigValues();
}
