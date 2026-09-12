package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class LegacyEnumSerializers {
	private LegacyEnumSerializers() {}

	public static <T extends Enum<T>> IConfigListValueSerializer<T> list(Class<T> enumClass) {
		return new EnumListSerializer<>(enumClass);
	}

	public static <T extends Enum<T>> IConfigValueSerializer<T> enumOrBoolean(
		Class<T> enumClass,
		Function<Boolean, T> legacyBooleanMigration
	) {
		return new EnumOrBooleanSerializer<>(enumClass, legacyBooleanMigration);
	}

	private record EnumOrBooleanSerializer<T extends Enum<T>>(
		Class<T> enumClass,
		Function<Boolean, T> legacyBooleanMigration
	) implements IConfigValueSerializer<T> {
		@Override
		public String serialize(T value) {
			return value.name();
		}

		@Override
		public IDeserializeResult<T> deserialize(String string) {
			String value = normalizeSerializedValue(string);
			try {
				return IDeserializeResult.success(Enum.valueOf(enumClass, value));
			} catch (IllegalArgumentException ignored) {
				try {
					return IDeserializeResult.success(legacyBooleanMigration.apply(parseBoolean(value)));
				} catch (IllegalArgumentException e) {
					return IDeserializeResult.failure(e.getMessage());
				}
			}
		}

		@Override
		public boolean isValid(@Nullable T value) {
			return value != null && value.getDeclaringClass() == enumClass;
		}

		@Override
		public Optional<List<T>> getAllValidValues() {
			return Optional.of(List.of(enumClass.getEnumConstants()));
		}

		@Override
		public String getValidValuesDescription() {
			return getEnumNames(enumClass);
		}
	}

	private record EnumListSerializer<T extends Enum<T>>(
		Class<T> enumClass
	) implements IConfigListValueSerializer<T> {
		@Override
		public String serialize(List<T> values) {
			return values.stream()
				.map(Enum::name)
				.collect(Collectors.joining(", "));
		}

		@Override
		public IDeserializeResult<List<T>> deserialize(String string) {
			try {
				boolean enabled = parseBoolean(string);
				if (enabled) {
					return IDeserializeResult.success(List.of(enumClass.getEnumConstants()));
				}
				return IDeserializeResult.success(List.of());
			} catch (IllegalArgumentException ignored) {
				// Continue with the legacy enum-list format.
			}

			String checkedString = string.trim();
			if (checkedString.startsWith("[")) {
				if (!checkedString.endsWith("]")) {
					String errorMessage = """
						No closing brace found.
						List must have no braces, or be wrapped in [ and ].""";
					return IDeserializeResult.failure(errorMessage);
				}
				checkedString = checkedString.substring(1, checkedString.length() - 1);
			}
			if (checkedString.isBlank()) {
				return IDeserializeResult.success(List.of());
			}
			String[] split = checkedString.split(",");

			List<String> diagnostics = new ArrayList<>();
			List<T> results = Arrays.stream(split)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(this::deserializeEnum)
				.<T>mapMulti((result, consumer) -> {
					result.getResult().ifPresent(consumer);
					diagnostics.addAll(result.getDiagnostics());
				})
				.toList();

			if (diagnostics.isEmpty()) {
				return IDeserializeResult.success(results);
			}
			if (results.isEmpty()) {
				return IDeserializeResult.failure(diagnostics);
			}
			return IDeserializeResult.partialSuccess(results, diagnostics);
		}

		@Override
		public IConfigValueSerializer<T> getElementSerializer() {
			return new EnumSerializer<>(enumClass);
		}

		private IDeserializeResult<T> deserializeEnum(String string) {
			return getElementSerializer().deserialize(string);
		}

		@Override
		public boolean isValid(List<T> values) {
			return values.stream()
				.allMatch(value -> value != null && value.getDeclaringClass() == enumClass);
		}

		@Override
		public Optional<List<List<T>>> getAllValidValues() {
			return Optional.empty();
		}

		@Override
		public String getValidValuesDescription() {
			return "A comma-separated list containing values of:\n[%s]".formatted(getEnumNames(enumClass));
		}
	}

	private record EnumSerializer<T extends Enum<T>>(
		Class<T> enumClass
	) implements IConfigValueSerializer<T> {
		@Override
		public String serialize(T value) {
			return value.name();
		}

		@Override
		public IDeserializeResult<T> deserialize(String string) {
			String enumName = normalizeSerializedValue(string);
			try {
				return IDeserializeResult.success(Enum.valueOf(enumClass, enumName));
			} catch (IllegalArgumentException ignored) {
				return IDeserializeResult.failure("Invalid enum name: No enum constant %s.%s".formatted(enumClass.getCanonicalName(), enumName));
			}
		}

		@Override
		public boolean isValid(@Nullable T value) {
			return value != null && value.getDeclaringClass() == enumClass;
		}

		@Override
		public Optional<List<T>> getAllValidValues() {
			return Optional.of(List.of(enumClass.getEnumConstants()));
		}

		@Override
		public String getValidValuesDescription() {
			return getEnumNames(enumClass);
		}
	}

	private static boolean parseBoolean(String string) {
		String value = normalizeSerializedValue(string);
		if ("true".equalsIgnoreCase(value)) {
			return true;
		}
		if ("false".equalsIgnoreCase(value)) {
			return false;
		}
		throw new IllegalArgumentException("Invalid boolean value. Must be true or false.");
	}

	private static String normalizeSerializedValue(String string) {
		String value = string.trim();
		if (value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1);
		}
		return value;
	}

	private static <T extends Enum<T>> String getEnumNames(Class<T> enumClass) {
		return Arrays.stream(enumClass.getEnumConstants())
			.map(Enum::name)
			.collect(Collectors.joining(", "));
	}
}
