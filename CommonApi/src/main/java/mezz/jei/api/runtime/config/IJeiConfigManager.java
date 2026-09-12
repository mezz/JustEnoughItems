package mezz.jei.api.runtime.config;

import mezz.jei.api.runtime.IJeiRuntime;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * Gives access to JEI's config files.
 * Useful for mods that let users change configs in-game.
 *
 * Get an instance from {@link IJeiRuntime#getConfigManager()}
 *
 * @since 12.1.0
 * @deprecated use {@code net.mezzdev.config.api.Configs#getSchemas()}
 */
@Deprecated(since = "19.55.0", forRemoval = true)
@ApiStatus.NonExtendable
@SuppressWarnings({"removal", "DeprecatedIsStillUsed"})
public interface IJeiConfigManager {
	/**
	 * @return all of JEI's config files.
	 * @see IJeiConfigFile
	 *
	 * @since 12.1.0
	 */
	@Unmodifiable
	Collection<IJeiConfigFile> getConfigFiles();
}
