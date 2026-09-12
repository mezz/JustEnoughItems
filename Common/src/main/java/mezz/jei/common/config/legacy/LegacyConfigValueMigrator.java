package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.migration.IConfigMigrationContext;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a value written by JEI's config system and applies it through MezzConfig.
 * MezzConfig remains responsible for validating and saving its own file format.
 */
public final class LegacyConfigValueMigrator {
	private LegacyConfigValueMigrator() {}

	public static <T> void register(
		IConfigSchemaBuilder schema,
		List<Path> legacyPaths,
		IConfigValue<T> configValue,
		String categoryName,
		String valueName,
		IConfigValueSerializer<T> serializer
	) {
		if (!legacyPaths.isEmpty()) {
			schema.setLegacyMigration(
				legacyPaths,
				(path, context) -> migrate(path, context, configValue, categoryName, valueName, serializer)
			);
		}
	}

	private static <T> void migrate(
		Path path,
		IConfigMigrationContext context,
		IConfigValue<T> configValue,
		String categoryName,
		String valueName,
		IConfigValueSerializer<T> serializer
	) throws IOException {
		T value = LegacyConfigValueLoader.loadValue(path, categoryName, valueName, serializer);
		context.set(configValue, value);
	}
}
