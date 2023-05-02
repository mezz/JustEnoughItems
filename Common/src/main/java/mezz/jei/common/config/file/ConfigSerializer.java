package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.core.util.PathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigSerializer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern commentRegex = Pattern.compile("\\s*#.*");
    private static final Pattern categoryRegex = Pattern.compile("\\[(?<category>\\w+)]\\s*");
    private static final Pattern keyValueRegex = Pattern.compile("\\s*(?<key>\\w+)\\s*=\\s*(?<value>.*)");

    private static String getLineErrorString(Path path, int lineNumber, String line, String errorMessage) {
        return """
               %s
               Config file: %s
               Line #%s: "%s\"""".formatted(errorMessage, path, lineNumber, line);
    }

    public static void load(Path path, @Unmodifiable List<ConfigCategory> categories) throws IOException {
        LOGGER.debug("Loading config file: {}", path);
        List<String> lines = Files.readAllLines(path);

        Map<String, ConfigCategory> categoriesMap = new LinkedHashMap<>();
        for (ConfigCategory category : categories) {
            categoriesMap.put(category.getName(), category);
        }

        ConfigCategory category = null;
        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = lines.get(i);
            if (line.isBlank() || commentRegex.matcher(line).matches()) {
                continue;
            }
            Matcher categoryMatcher = categoryRegex.matcher(line);
            if (categoryMatcher.matches()) {
                String categoryName = categoryMatcher.group("category");
                category = categoriesMap.get(categoryName);
                if (category == null) {
                    LOGGER.error(getLineErrorString(path, lineNumber, line,
                        """
                        '[%s]' is not a valid category name.
                        Valid names are: [%s]
                        Skipping all values until the first valid category is declared."""
                        .formatted(
                            categoryName,
                            String.join(", ", categoriesMap.keySet())
                        )
                    ));
                }
                continue;
            }
            if (category == null) {
                LOGGER.error(getLineErrorString(path, lineNumber, line, """
                Expected a '[category]' here.
                Configs must start with a category before defining values.
                Skipping all lines until the first valid category is declared."""));
                continue;
            }

            Matcher keyValueMatcher = keyValueRegex.matcher(line);
            if (keyValueMatcher.matches()) {
                final String key = keyValueMatcher.group("key").trim();
                final String value = keyValueMatcher.group("value").trim();
                Optional<ConfigValue<?>> configValue = category.getConfigValue(key);
                if (configValue.isEmpty()) {
                    LOGGER.error(getLineErrorString(path, lineNumber, line,
                        """
                        '%s' is not a valid config key for config category '%s'.
                        Valid keys: [%s]
                        Skipping this key."""
                        .formatted(
                            key, category.getName(),
                            String.join(", ", category.getValueNames())
                        )
                    ));
                } else {
                    List<String> errors = configValue.get().setFromSerializedValue(value);
                    if (!errors.isEmpty()) {
                        String errorMessage = """
                            Encountered Errors when deserializing value '%s':
                            %s""".formatted(value, String.join("\n", errors));
                        LOGGER.error(getLineErrorString(path, lineNumber, line, errorMessage));
                    }
                }
            } else {
                LOGGER.error(getLineErrorString(path, lineNumber, line,
                    """
                        Encountered an invalid line.
                        Every line in the config must be either:
                        * a '[category]'
                        * a 'key = value' pair
                        * a '#'-prefixed comment"""
                ));
            }
        }
    }

    public static void save(Path path, List<ConfigCategory> categories) throws IOException {
        List<String> serialized = new ArrayList<>();
        categories.forEach(category -> {
            serializeCategory(serialized, category);
            serialized.add("");
        });
        LOGGER.debug("Saving config file: {}", path);
        PathUtil.writeUsingTempFile(path, serialized);
    }

    private static void serializeCategory(List<String> serialized, ConfigCategory category) {
        serialized.add("[%s]".formatted(category.getName()));
        for (ConfigValue<?> value : category.getConfigValues()) {
            serializeConfigValue(serialized, value);
            serialized.add("");
        }
    }

    private static <T> void serializeConfigValue(List<String> serialized, ConfigValue<T> configValue) {
        String name = configValue.getName();
        IJeiConfigValueSerializer<T> serializer = configValue.getSerializer();

        String description = "Description: %s".formatted(configValue.getDescription());
        addCommentedStrings(serialized, description);

        String validValues = "Valid Values: %s".formatted(serializer.getValidValuesDescription());
        addCommentedStrings(serialized, validValues);

        T defaultValue = configValue.getDefaultValue();
        String defaultValueSerialized = serializer.serialize(defaultValue);
        String defaultValueString = "Default Value: %s".formatted(defaultValueSerialized);
        addCommentedStrings(serialized, defaultValueString);

        T value = configValue.getValue();
        String valueString = serializer.serialize(value);
        serialized.add("\t%s = %s".formatted(name, valueString));
    }

    private static void addCommentedStrings(List<String> serialized, String comment) {
        String[] lines = comment.split("\n");
        if (lines.length == 0) {
            return;
        }
        serialized.add("\t# %s".formatted(lines[0]));
        if (lines.length > 1) {
            for (int i = 1; i < lines.length; i++) {
                serialized.add("\t# %s".formatted(lines[i]));
            }
        }
    }
}
