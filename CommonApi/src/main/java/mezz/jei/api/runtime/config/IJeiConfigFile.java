package mezz.jei.api.runtime.config;

import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents one Config file used by JEI.
 *
 * Config files contain one or more {@link IJeiConfigCategory},
 * and each category has one or more {@link IJeiConfigValue}.
 *
 * @since 12.1.0
 */
public interface IJeiConfigFile {
    /**
     * Get the path of this config file.
     * Used for differentiating between config files.
     *
     * Note that config values will read from this file automatically,
     * and updating config values will save the file automatically,
     * so you should not read or write this file yourself.
     *
     * @since 12.1.0
     */
    Path getPath();

    /**
     * Get all the categories in this file.
     * Each category contains values that can be read or edited.
     *
     * @since 12.1.0
     */
    @Unmodifiable
    List<? extends IJeiConfigCategory> getCategories();
}
