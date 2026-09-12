package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LegacyConfigValueLoader {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern CATEGORY_PATTERN = Pattern.compile("\\s*\\[(?<category>\\w+)]\\s*");
	private static final Pattern VALUE_PATTERN = Pattern.compile("\\s*(?<key>\\w+)\\s*=\\s*(?<value>.*)");

	private LegacyConfigValueLoader() {}

	public static <T> T loadValue(
		Path path,
		String targetCategoryName,
		String targetValueName,
		IConfigValueSerializer<T> serializer
	) throws IOException {
		path = Objects.requireNonNull(path).toAbsolutePath().normalize();
		Objects.requireNonNull(targetCategoryName);
		Objects.requireNonNull(targetValueName);
		Objects.requireNonNull(serializer);
		return parseValue(path, Files.readAllLines(path), targetCategoryName, targetValueName, serializer);
	}

	private static <T> T parseValue(
		Path path,
		List<String> lines,
		String targetCategoryName,
		String targetValueName,
		IConfigValueSerializer<T> serializer
	) {
		String categoryName = "";
		String serializedValue = null;
		for (String line : lines) {
			Matcher categoryMatcher = CATEGORY_PATTERN.matcher(line);
			if (categoryMatcher.matches()) {
				categoryName = categoryMatcher.group("category");
				continue;
			}
			if (!targetCategoryName.equals(categoryName)) {
				continue;
			}
			Matcher valueMatcher = VALUE_PATTERN.matcher(line);
			if (valueMatcher.matches() && targetValueName.equals(valueMatcher.group("key"))) {
				serializedValue = valueMatcher.group("value").trim();
			}
		}

		if (serializedValue == null) {
			throw new IllegalArgumentException(
				"Legacy JEI config '%s' has no %s.%s value.".formatted(path, targetCategoryName, targetValueName)
			);
		}

		IDeserializeResult<T> result = serializer.deserialize(serializedValue);
		T value = result.getResult().orElse(null);
		if (value == null) {
			throw new IllegalArgumentException(
				"Failed to parse legacy JEI config '%s': %s"
					.formatted(path, String.join("; ", result.getDiagnostics()))
			);
		}
		if (!result.getDiagnostics().isEmpty()) {
			LOGGER.warn(
				"Partially parsed legacy JEI config '{}': {}",
				path,
				String.join("; ", result.getDiagnostics())
			);
		}
		return value;
	}
}
