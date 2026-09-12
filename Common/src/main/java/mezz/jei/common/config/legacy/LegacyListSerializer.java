package mezz.jei.common.config.legacy;

import net.mezzdev.config.api.value.serializer.ConfigListOrdering;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reads comma-separated lists written by JEI's config system before MezzConfig.
 * This is only used to load legacy values before handing them to current MezzConfig values.
 */
public final class LegacyListSerializer<T> implements IConfigListValueSerializer<T> {
	private final IConfigValueSerializer<T> elementSerializer;
	private final ConfigListOrdering ordering;

	public LegacyListSerializer(IConfigValueSerializer<T> elementSerializer, ConfigListOrdering ordering) {
		this.elementSerializer = Objects.requireNonNull(elementSerializer);
		this.ordering = Objects.requireNonNull(ordering);
	}

	@Override
	public String serialize(List<T> values) {
		return values.stream()
			.map(elementSerializer::serialize)
			.collect(Collectors.joining(", "));
	}

	@Override
	public IDeserializeResult<List<T>> deserialize(String string) {
		List<String> diagnostics = new ArrayList<>();
		List<T> results = Arrays.stream(string.split(","))
			.map(String::trim)
			.filter(value -> !value.isEmpty())
			.map(elementSerializer::deserialize)
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
	public boolean isValid(List<T> values) {
		return values != null && values.stream().allMatch(elementSerializer::isValid);
	}

	@Override
	public String getValidValuesDescription() {
		return "A list containing values of:\n%s".formatted(elementSerializer.getValidValuesDescription());
	}

	@Override
	public ConfigListOrdering getOrdering() {
		return ordering;
	}

	@Override
	public IConfigValueSerializer<T> getElementSerializer() {
		return elementSerializer;
	}
}
