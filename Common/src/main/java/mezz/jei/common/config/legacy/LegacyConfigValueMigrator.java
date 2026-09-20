package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.migration.IConfigMigrationContext;
import net.mezzdev.config.api.migration.IConfigMigrationResult;
import net.mezzdev.config.api.migration.IConfigMigrator;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a value written by JEI's config system and applies it through MezzConfig.
 * MezzConfig remains responsible for validating and saving its own file format.
 */
public final class LegacyConfigValueMigrator {
	private static final Logger LOGGER = LogManager.getLogger();

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
				new ValueMigrator<>(configValue, categoryName, valueName, serializer)
			);
		}
	}

	private record ValueMigrator<T>(
		IConfigValue<T> configValue,
		String categoryName,
		String valueName,
		IConfigValueSerializer<T> serializer
	) implements IConfigMigrator {
		@Override
		public void migrate(Path path, IConfigMigrationContext context) throws IOException {
			IDeserializeResult<T> result = LegacyConfigValueLoader.loadValue(path, categoryName, valueName, serializer);
			result.getResult().ifPresent(value -> context.set(configValue, value));

			List<String> diagnostics = result.getDiagnostics();
			if (!diagnostics.isEmpty()) {
				context.rejectValue(
					"Failed to fully migrate legacy JEI config value '%s.%s' from '%s': %s"
						.formatted(categoryName, valueName, path, String.join("; ", diagnostics))
				);
			}
		}

		@Override
		public void onMigrationComplete(IConfigMigrationResult result) {
			if (result.getRejectedValueCount() > 0) {
				LOGGER.warn(
					"Legacy JEI config migration imported {} value(s) and rejected {} value(s): {}",
					result.getImportedValueCount(),
					result.getRejectedValueCount(),
					String.join("; ", result.getDiagnostics())
				);
			}
		}
	}
}
