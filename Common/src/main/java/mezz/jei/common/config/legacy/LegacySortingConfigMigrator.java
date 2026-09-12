package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.sorting.ISortingConfig;
import net.mezzdev.config.api.migration.ISortingConfigMigrationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Loads a sort order written by JEI's config system and applies it through MezzConfig.
 * MezzConfig remains responsible for validating and saving its own file format.
 */
public final class LegacySortingConfigMigrator {
	private LegacySortingConfigMigrator() {}

	public static ISortingConfig<String> register(
		ISortingConfig<String> sortingConfig,
		Path jeiConfigDirectory,
		UUID profileId,
		String configFileName
	) {
		return sortingConfig.setLegacyMigration(
			LegacyConfigPaths.get(jeiConfigDirectory, profileId, configFileName),
			LegacySortingConfigMigrator::migrate
		);
	}

	private static void migrate(Path path, ISortingConfigMigrationContext<String> context) throws IOException {
		LinkedHashSet<String> legacySortedValues = new LinkedHashSet<>();
		Files.readAllLines(path).stream()
			.filter(value -> !value.isBlank())
			.forEach(legacySortedValues::add);
		List<String> values = List.copyOf(legacySortedValues);
		context.setSortedValues(values, values);
	}
}
