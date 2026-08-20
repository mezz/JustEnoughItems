package mezz.jei.library.ingredients;

import mezz.jei.api.ingredients.ISlotDisplayInterpretationBuilder;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class SlotDisplayInterpretationBuilder<T> implements ISlotDisplayInterpretationBuilder<T> {
	private SlotDisplayInfo.Value<Boolean> matchesAllSubtypes = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<Boolean> wildcardForSubtypes = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<TagKey<?>> tagKey = SlotDisplayInfo.Value.unspecified();
	private SlotDisplayInfo.Value<Component> tooltipHeader = SlotDisplayInfo.Value.unspecified();
	private final List<ChildDisplay<T>> childDisplays = new ArrayList<>();
	private boolean childDisplaysSet;

	@Override
	@SuppressWarnings("removal")
	@Deprecated(since = "29.30.0", forRemoval = true)
	public ISlotDisplayInterpretationBuilder<T> setWrappedDisplay(SlotDisplay wrappedDisplay) {
		ErrorUtil.checkNotNull(wrappedDisplay, "wrappedDisplay");
		this.childDisplays.clear();
		this.childDisplays.add(new ChildDisplay<>(wrappedDisplay, UnaryOperator.identity()));
		this.childDisplaysSet = true;
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	@Deprecated(since = "29.30.0", forRemoval = true)
	public ISlotDisplayInterpretationBuilder<T> setChildDisplays(List<? extends SlotDisplay> childDisplays) {
		ErrorUtil.checkNotNull(childDisplays, "childDisplays");
		List<ChildDisplay<T>> newChildDisplays = createChildDisplays(childDisplays);
		this.childDisplays.clear();
		this.childDisplays.addAll(newChildDisplays);
		this.childDisplaysSet = true;
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> addChildDisplay(SlotDisplay childDisplay) {
		return addChildDisplay(childDisplay, UnaryOperator.identity());
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> addChildDisplay(
		SlotDisplay childDisplay,
		UnaryOperator<T> ingredientTransformer
	) {
		ErrorUtil.checkNotNull(childDisplay, "childDisplay");
		ErrorUtil.checkNotNull(ingredientTransformer, "ingredientTransformer");
		this.childDisplays.add(new ChildDisplay<>(childDisplay, ingredientTransformer));
		this.childDisplaysSet = true;
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> setWildcardForSubtypes(boolean wildcardForSubtypes) {
		this.wildcardForSubtypes = SlotDisplayInfo.Value.of(wildcardForSubtypes);
		return this;
	}

	@Override
	@SuppressWarnings("removal")
	@Deprecated(since = "29.26.0", forRemoval = true)
	public ISlotDisplayInterpretationBuilder<T> setMatchesAllSubtypes(boolean matchesAllSubtypes) {
		this.matchesAllSubtypes = SlotDisplayInfo.Value.of(matchesAllSubtypes);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> setTagKey(TagKey<?> tagKey) {
		ErrorUtil.checkNotNull(tagKey, "tagKey");
		this.tagKey = SlotDisplayInfo.Value.of(tagKey);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> clearTagKey() {
		this.tagKey = SlotDisplayInfo.Value.empty();
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> setTooltipHeader(Component tooltipHeader) {
		ErrorUtil.checkNotNull(tooltipHeader, "tooltipHeader");
		this.tooltipHeader = SlotDisplayInfo.Value.of(tooltipHeader);
		return this;
	}

	@Override
	public ISlotDisplayInterpretationBuilder<T> clearTooltipHeader() {
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

	List<ChildDisplay<T>> getChildDisplays() {
		return childDisplays;
	}

	boolean isChildDisplaysSet() {
		return childDisplaysSet;
	}

	private static <T> List<ChildDisplay<T>> createChildDisplays(List<? extends SlotDisplay> childDisplays) {
		return childDisplays.stream()
			.map(childDisplay -> new ChildDisplay<T>(childDisplay, UnaryOperator.identity()))
			.toList();
	}

	record ChildDisplay<T>(SlotDisplay slotDisplay, UnaryOperator<T> ingredientTransformer) {
		ChildDisplay {
			ErrorUtil.checkNotNull(slotDisplay, "slotDisplay");
			ErrorUtil.checkNotNull(ingredientTransformer, "ingredientTransformer");
		}
	}
}
