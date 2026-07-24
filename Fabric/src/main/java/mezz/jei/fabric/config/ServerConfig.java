package mezz.jei.fabric.config;

import mezz.jei.core.config.IServerConfig;
import mezz.jei.common.util.PathUtil;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ServerConfig implements IServerConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String CONFIG_FILE_NAME = "jei-server.properties";

	private static final ConfigOption ENABLE_CHEAT_MODE_FOR_OP = new ConfigOption(
		"enableCheatModeForOp",
		true,
		"Enable the cheat mode for players who have an operator status (/op)."
	);
	private static final ConfigOption ENABLE_CHEAT_MODE_FOR_CREATIVE = new ConfigOption(
		"enableCheatModeForCreative",
		true,
		"Enable the cheat mode for players who are in the creative mode."
	);
	private static final ConfigOption ENABLE_CHEAT_MODE_FOR_GIVE = new ConfigOption(
		"enableCheatModeForGive",
		false,
		"Enable the cheat mode for players who can use the \"/give\" command."
	);

	@Nullable
	private static ServerConfig INSTANCE;

	private final boolean enableCheatModeForOp;
	private final boolean enableCheatModeForCreative;
	private final boolean enableCheatModeForGive;

	public static ServerConfig getInstance() {
		if (INSTANCE == null) {
			Path configFile = FabricLoader.getInstance()
				.getConfigDir()
				.resolve(CONFIG_FILE_NAME);
			INSTANCE = new ServerConfig(configFile);
		}
		return INSTANCE;
	}

	private ServerConfig(Path configFile) {
		Map<String, String> values = loadValues(configFile);
		this.enableCheatModeForOp = getBoolean(values, ENABLE_CHEAT_MODE_FOR_OP);
		this.enableCheatModeForCreative = getBoolean(values, ENABLE_CHEAT_MODE_FOR_CREATIVE);
		this.enableCheatModeForGive = getBoolean(values, ENABLE_CHEAT_MODE_FOR_GIVE);
		save(configFile);
	}

	private static Map<String, String> loadValues(Path configFile) {
		Map<String, String> values = new LinkedHashMap<>();
		if (!Files.exists(configFile)) {
			return values;
		}
		try {
			List<String> lines = Files.readAllLines(configFile);
			for (int i = 0; i < lines.size(); i++) {
				int lineNumber = i + 1;
				String line = lines.get(i).trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int separatorIndex = line.indexOf('=');
				if (separatorIndex == -1) {
					LOGGER.warn("Ignoring invalid Fabric JEI server config line {} in '{}': {}", lineNumber, configFile, line);
					continue;
				}
				String key = line.substring(0, separatorIndex).trim();
				String value = line.substring(separatorIndex + 1).trim();
				values.put(key, value);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load Fabric JEI server config: '{}'", configFile, e);
		}
		return values;
	}

	private static boolean getBoolean(Map<String, String> values, ConfigOption option) {
		String value = values.get(option.name());
		if (value == null || value.isBlank()) {
			return option.defaultValue();
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "true" -> true;
			case "false" -> false;
			default -> {
				LOGGER.warn(
					"Invalid boolean value '{}' for Fabric JEI server config '{}'. Using default value: {}",
					value,
					option.name(),
					option.defaultValue()
				);
				yield option.defaultValue();
			}
		};
	}

	private void save(Path configFile) {
		try {
			List<String> lines = serialize();
			PathUtil.writeUsingTempFile(configFile, lines);
		} catch (IOException e) {
			LOGGER.error("Failed to save Fabric JEI server config: '{}'", configFile, e);
		}
	}

	private List<String> serialize() {
		List<String> lines = new ArrayList<>();
		lines.add("# JEI server config");
		lines.add("# Boolean values accept true or false.");
		lines.add("");
		addOption(lines, ENABLE_CHEAT_MODE_FOR_OP, enableCheatModeForOp);
		addOption(lines, ENABLE_CHEAT_MODE_FOR_CREATIVE, enableCheatModeForCreative);
		addOption(lines, ENABLE_CHEAT_MODE_FOR_GIVE, enableCheatModeForGive);
		return lines;
	}

	private static void addOption(List<String> lines, ConfigOption option, boolean value) {
		lines.add("# " + option.comment());
		lines.add(option.name() + " = " + value);
		lines.add("");
	}

	@Override
	public boolean isCheatModeEnabledForOp() {
		return enableCheatModeForOp;
	}

	@Override
	public boolean isCheatModeEnabledForCreative() {
		return enableCheatModeForCreative;
	}

	@Override
	public boolean isCheatModeEnabledForGive() {
		return enableCheatModeForGive;
	}

	private record ConfigOption(String name, boolean defaultValue, String comment) {}
}
