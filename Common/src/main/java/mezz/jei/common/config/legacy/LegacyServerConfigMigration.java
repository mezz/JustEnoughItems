package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.api.value.IConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Imports the three booleans from JEI's old NeoForge TOML and Fabric properties files. */
public record LegacyServerConfigMigration(Path destination, Path source, Map<String, Boolean> values) {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<String> VALUE_NAMES = Set.of(
		"enableCheatModeForOp", "enableCheatModeForCreative", "enableCheatModeForGive"
	);

	public LegacyServerConfigMigration {
		destination = destination.toAbsolutePath().normalize();
		values = Map.copyOf(values);
	}

	public static Optional<LegacyServerConfigMigration> prepare(Path destination, List<Path> legacyPaths) {
		if (Files.exists(destination)) {
			return Optional.empty();
		}
		for (Path legacyPath : legacyPaths) {
			if (Files.exists(legacyPath)) {
				try {
					return Optional.of(new LegacyServerConfigMigration(destination, legacyPath, read(legacyPath)));
				} catch (IOException e) {
					throw new UncheckedIOException("Failed to read legacy JEI server config: " + legacyPath, e);
				}
			}
		}
		return Optional.empty();
	}

	private static Map<String, Boolean> read(Path path) throws IOException {
		Map<String, Boolean> values = new LinkedHashMap<>();
		boolean inCheatMode = path.getFileName().toString().endsWith(".properties");
		for (String line : Files.readAllLines(path)) {
			int comment = line.indexOf('#');
			if (comment >= 0) {
				line = line.substring(0, comment);
			}
			line = line.trim();
			if (line.startsWith("[")) {
				inCheatMode = line.endsWith("]") &&
					unquote(line.substring(1, line.length() - 1).trim()).equals("cheat mode");
				continue;
			}
			int separator = line.indexOf('=');
			if (!inCheatMode || separator < 0) {
				continue;
			}
			String name = unquote(line.substring(0, separator).trim());
			if (!VALUE_NAMES.contains(name)) {
				continue;
			}
			String value = line.substring(separator + 1).trim();
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
				values.put(name, Boolean.parseBoolean(value));
			} else {
				LOGGER.warn("Ignoring invalid legacy JEI server config value '{} = {}' in '{}'", name, value, path);
			}
		}
		return values;
	}

	private static String unquote(String name) {
		if (name.length() >= 2 &&
			((name.startsWith("\"") && name.endsWith("\"")) || (name.startsWith("'") && name.endsWith("'")))
		) {
			return name.substring(1, name.length() - 1);
		}
		return name;
	}

	public void apply(IConfigSchema schema, List<IConfigValue<Boolean>> configValues) {
		Path activePath = schema.getPath().orElseThrow(() -> new IllegalStateException("JEI server config is not active"));
		if (!destination.equals(activePath.toAbsolutePath().normalize())) {
			throw new IllegalStateException("JEI server config migration belongs to another world: " + destination);
		}
		schema.batchUpdate(updater -> {
			for (IConfigValue<Boolean> configValue : configValues) {
				Boolean value = values.get(configValue.getEditorInfo().getName());
				if (value != null) {
					updater.set(configValue, value);
				}
			}
		});
		LOGGER.info("Imported legacy JEI server config from '{}' into '{}'. The original file was preserved.", source, destination);
	}
}
