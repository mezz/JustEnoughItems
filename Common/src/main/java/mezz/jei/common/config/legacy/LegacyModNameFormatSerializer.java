package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LegacyModNameFormatSerializer implements IConfigListValueSerializer<ChatFormatting> {
	private final List<ChatFormatting> validValues;
	private final IConfigValueSerializer<ChatFormatting> elementSerializer;

	public LegacyModNameFormatSerializer(List<ChatFormatting> validValues) {
		this.validValues = List.copyOf(Objects.requireNonNull(validValues));
		this.elementSerializer = new ChatFormattingSerializer();
	}

	@Override
	public IConfigValueSerializer<ChatFormatting> getElementSerializer() {
		return elementSerializer;
	}

	@Override
	public String serialize(List<ChatFormatting> values) {
		return values.stream()
			.map(elementSerializer::serialize)
			.collect(Collectors.joining(", "));
	}

	@Override
	public IDeserializeResult<List<ChatFormatting>> deserialize(String string) {
		String value = normalizeSerializedValue(string);
		if (value.isEmpty()) {
			return IDeserializeResult.success(List.of());
		}

		String separator = getListSeparator(value);
		List<String> diagnostics = new ArrayList<>();
		List<ChatFormatting> results = Arrays.stream(value.split(separator))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(elementSerializer::deserialize)
			.<ChatFormatting>mapMulti((result, consumer) -> {
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
	public boolean isValid(List<ChatFormatting> values) {
		return values.stream()
			.allMatch(elementSerializer::isValid);
	}

	@Override
	public String getValidValuesDescription() {
		return "A comma-separated list containing values of:\n%s".formatted(elementSerializer.getValidValuesDescription());
	}

	private final class ChatFormattingSerializer implements IConfigValueSerializer<ChatFormatting> {
		@Override
		public String serialize(ChatFormatting value) {
			return value.name();
		}

		@Override
		public IDeserializeResult<ChatFormatting> deserialize(String string) {
			String value = normalizeSerializedValue(string);
			ChatFormatting chatFormatting = ChatFormatting.getByName(value);
			if (chatFormatting == null) {
				return IDeserializeResult.failure("No Chat Formatting found for name: '%s'".formatted(value));
			}
			if (!isValid(chatFormatting)) {
				return IDeserializeResult.failure("Chat Formatting '%s' is not valid".formatted(value));
			}
			return IDeserializeResult.success(chatFormatting);
		}

		@Override
		public boolean isValid(@Nullable ChatFormatting value) {
			return validValues.contains(value);
		}

		@Override
		public Optional<List<ChatFormatting>> getAllValidValues() {
			return Optional.of(validValues);
		}

		@Override
		public String getValidValuesDescription() {
			return validValues.stream()
				.map(ChatFormatting::name)
				.collect(Collectors.joining(", "));
		}
	}

	private static String getListSeparator(String value) {
		if (value.contains(",")) {
			return ",";
		}
		return "\\s+";
	}

	private static String normalizeSerializedValue(String value) {
		value = value.trim();
		if (value.startsWith("[") && value.endsWith("]")) {
			value = value.substring(1, value.length() - 1)
				.trim();
		}
		if (value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1)
				.trim();
		}
		return value;
	}
}
