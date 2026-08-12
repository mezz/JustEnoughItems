package mezz.jei.library.ingredients;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class SlotDisplayInfo {
	public static final SlotDisplayInfo EMPTY = new SlotDisplayInfo(
		Value.unspecified(),
		Value.unspecified(),
		Value.unspecified()
	);

	private final Value<Boolean> matchesAllSubtypes;
	private final Value<TagKey<?>> tagKey;
	private final Value<Component> tooltipHeader;

	SlotDisplayInfo(
		Value<Boolean> matchesAllSubtypes,
		Value<TagKey<?>> tagKey,
		Value<Component> tooltipHeader
	) {
		this.matchesAllSubtypes = Objects.requireNonNull(matchesAllSubtypes);
		this.tagKey = Objects.requireNonNull(tagKey);
		this.tooltipHeader = Objects.requireNonNull(tooltipHeader);
	}

	public boolean matchesAllSubtypes() {
		return matchesAllSubtypes.value().orElse(false);
	}

	public Optional<TagKey<?>> tagKey() {
		return tagKey.value();
	}

	public Optional<TagKey<?>> tagKeyOrElse(Supplier<Optional<TagKey<?>>> fallback) {
		Objects.requireNonNull(fallback);
		if (tagKey.specified()) {
			return tagKey.value();
		}
		return Objects.requireNonNull(fallback.get());
	}

	public Optional<Component> tooltipHeader() {
		return tooltipHeader.value();
	}

	SlotDisplayInfo overlayOn(SlotDisplayInfo childInfo) {
		return new SlotDisplayInfo(
			matchesAllSubtypes.overlayOn(childInfo.matchesAllSubtypes),
			tagKey.overlayOn(childInfo.tagKey),
			tooltipHeader.overlayOn(childInfo.tooltipHeader)
		);
	}

	public boolean isEmpty() {
		return !matchesAllSubtypes.specified() && !tagKey.specified() && !tooltipHeader.specified();
	}

	record Value<T>(boolean specified, Optional<T> value) {
		Value {
			Objects.requireNonNull(value);
		}

		static <T> Value<T> unspecified() {
			return new Value<>(false, Optional.empty());
		}

		static <T> Value<T> of(T value) {
			return new Value<>(true, Optional.of(value));
		}

		static <T> Value<T> empty() {
			return new Value<>(true, Optional.empty());
		}

		Value<T> overlayOn(Value<T> childValue) {
			if (specified) {
				return this;
			}
			return childValue;
		}
	}
}
