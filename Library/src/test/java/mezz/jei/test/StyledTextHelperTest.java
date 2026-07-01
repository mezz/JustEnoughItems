package mezz.jei.test;

import mezz.jei.library.config.StyledTextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StyledTextHelperTest {
	private static final String TARGET = "Target";
	private static final String REPLACEMENT = "<replacement>";
	private static final Component REPLACEMENT_COMPONENT = Component.literal(REPLACEMENT);

	@Test
	public void replaceFirstReturnsEmptyWhenTargetIsMissing() {
		// Setup: a styled component does not contain the target text.
		Component text = Component.literal("No matching text")
			.withStyle(ChatFormatting.BLUE);

		// Operation: try to replace a target that is not present.
		Optional<Component> result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT_COMPONENT);

		// Assertions: no replacement component is produced.
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void replaceFirstReplacesUnstyledText() {
		// Setup: the target text is present with no styling.
		Component text = Component.literal(TARGET);

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: the replacement is inserted with no style.
		Component expected = Component.literal(REPLACEMENT);
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstPreservesLegacyFormatting() {
		// Setup: the target text uses legacy formatting codes.
		Component text = Component.literal(ChatFormatting.BLUE + TARGET);

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: the legacy color is converted to component style on the replacement.
		Component expected = Component.literal(REPLACEMENT)
			.withStyle(ChatFormatting.BLUE);
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstPreservesComponentStyle() {
		// Setup: the target text uses component style instead of legacy formatting codes.
		Component text = Component.literal(TARGET)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: the component style is preserved on the replacement.
		Component expected = Component.literal(REPLACEMENT)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstPreservesLegacyAndComponentStyleTogether() {
		// Setup: the target text combines legacy color with direct component style.
		Component text = Component.literal(ChatFormatting.BLUE + TARGET)
			.withStyle(ChatFormatting.ITALIC);

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: both legacy and component styles are preserved on the replacement.
		Component expected = Component.literal(REPLACEMENT)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstPreservesStyleOutsideTarget() {
		// Setup: the prefix and target use different component styles.
		Component text = Component.empty()
			.append(Component.literal("Label: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(TARGET).withStyle(ChatFormatting.BLUE));

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: the prefix and target styles are both preserved.
		Component expected = Component.empty()
			.append(Component.literal("Label: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(REPLACEMENT).withStyle(ChatFormatting.BLUE));
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstPreservesStyleOnPrefixSegmentContainingTarget() {
		// Setup: part of the prefix is red, and the remaining prefix plus target are blue.
		Component text = Component.empty()
			.append(Component.literal("La").withStyle(ChatFormatting.RED))
			.append(Component.literal("bel: " + TARGET).withStyle(ChatFormatting.BLUE));

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: styled text before the target remains in the output.
		Component expected = Component.empty()
			.append(Component.literal("La").withStyle(ChatFormatting.RED))
			.append(Component.literal("bel: " + REPLACEMENT).withStyle(ChatFormatting.BLUE));
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstUsesSharedStyleWhenTargetSpansMatchingStyledComponents() {
		// Setup: the target text is split across two components with the same style.
		Component text = Component.empty()
			.append(Component.literal("Tar").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
			.append(Component.literal("get").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: the shared style is applied once to the replacement.
		Component expected = Component.literal(REPLACEMENT)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
		assertComponentsEqual(expected, result);
	}

	@Test
	public void replaceFirstDropsStyleWhenTargetSpansDifferentStyledComponents() {
		// Setup: the target text is split across components with different styles.
		Component text = Component.empty()
			.append(Component.literal("Tar").withStyle(ChatFormatting.BLUE))
			.append(Component.literal("get").withStyle(ChatFormatting.ITALIC));

		// Operation: replace the target text.
		Component result = replaceFirst(text);

		// Assertions: inconsistent target styles are not collapsed into one replacement style.
		Component expected = Component.literal(REPLACEMENT);
		assertComponentsEqual(expected, result);
	}

	@Test
	public void toLegacyStringResetsFormattingWhenReturningToUnstyledText() {
		// Setup: styled prefix text is followed by unstyled text.
		Component text = Component.empty()
			.append(Component.literal("Label: ").withStyle(ChatFormatting.RED))
			.append(Component.literal(TARGET));

		// Operation: serialize the component into legacy formatting codes.
		String result = StyledTextHelper.toLegacyString(text);

		// Assertions: a reset code is emitted before the unstyled text.
		Assertions.assertEquals(ChatFormatting.RED + "Label: " + ChatFormatting.RESET + TARGET, result);
	}

	private static Component replaceFirst(Component text) {
		return StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT_COMPONENT)
			.orElseThrow();
	}

	private static void assertComponentsEqual(Component expected, Component result) {
		Assertions.assertEquals(getStyledTexts(expected), getStyledTexts(result));
	}

	private static List<StyledText> getStyledTexts(Component component) {
		List<StyledText> styledTexts = new ArrayList<>();
		component.visit((style, text) -> {
			if (!text.isEmpty()) {
				addStyledText(styledTexts, text, style);
			}
			return Optional.empty();
		}, Style.EMPTY);
		return styledTexts;
	}

	private static void addStyledText(List<StyledText> styledTexts, String text, Style style) {
		if (!styledTexts.isEmpty()) {
			StyledText previous = styledTexts.getLast();
			if (Objects.equals(previous.style(), style)) {
				styledTexts.set(styledTexts.size() - 1, new StyledText(previous.text() + text, style));
				return;
			}
		}
		styledTexts.add(new StyledText(text, style));
	}

	private record StyledText(String text, Style style) {
	}
}
