package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.ISlotDisplayInterpretationBuilder;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

final class SlotDisplayInterpretationBuilder implements ISlotDisplayInterpretationBuilder {
	private SlotDisplayInfo.Value<Boolean> matchesAllSubtypes = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<Boolean> wildcardForSubtypes = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<TagKey<?>> tagKey = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<Component> tooltipHeader = SlotDisplayInfo.Value.unspecified();
	private @Nullable List<SlotDisplay> childDisplays;

	@Override
	public ISlotDisplayInterpretationBuilder setWrappedDisplay(SlotDisplay wrappedDisplay) {
		ErrorUtil.checkNotNull(wrappedDisplay, "wrappedDisplay");
		return setChildDisplays(List.of(wrappedDisplay));
	}

	@Override
	public ISlotDisplayInterpretationBuilder setChildDisplays(List<? extends SlotDisplay> childDisplays) {
		ErrorUtil.checkNotNull(childDisplays, "childDisplays");
		this.childDisplays = List.copyOf(childDisplays);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder setWildcardForSubtypes(boolean wildcardForSubtypes) {
		this.wildcardForSubtypes = SlotDisplayInfo.Value.of(wildcardForSubtypes);
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	@Deprecated(since = "27.26.0", forRemoval = true)
	public ISlotDisplayInterpretationBuilder setMatchesAllSubtypes(boolean matchesAllSubtypes) {
		this.matchesAllSubtypes = SlotDisplayInfo.Value.of(matchesAllSubtypes);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder setTagKey(TagKey<?> tagKey) {
		ErrorUtil.checkNotNull(tagKey, "tagKey");
		this.tagKey = SlotDisplayInfo.Value.of(tagKey);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder clearTagKey() {
		this.tagKey = SlotDisplayInfo.Value.empty();
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder setTooltipHeader(Component tooltipHeader) {
		ErrorUtil.checkNotNull(tooltipHeader, "tooltipHeader");
		this.tooltipHeader = SlotDisplayInfo.Value.of(tooltipHeader);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder clearTooltipHeader() {
		this.tooltipHeader = SlotDisplayInfo.Value.empty();
		return this;
	}

	SlotDisplayInfo buildInfo() {
		return new SlotDisplayInfo(
			matchesAllSubtypes,
			wildcardForSubtypes,
			tagKey,
			tooltipHeader
		);
	}

	Optional<List<SlotDisplay>> getChildDisplays() {
		return Optional.ofNullable(childDisplays);
	}
}
