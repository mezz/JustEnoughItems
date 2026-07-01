package mezz.jei.test;

import mezz.jei.library.config.StyledTextHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class StyledTextHelperTest {
	private static final String TARGET = "Target";
	private static final String TARGET_START = "Tar";
	private static final String TARGET_END = "get";
	private static final String REPLACEMENT = "<replacement>";
	private static final String LABEL = "Label: ";

	@Test
	public void testReplaceFirstWithNoMatch() {
		// Setup: the component has formatting but does not contain the target text.
		Component text = Component.literal("No matching text")
			.withStyle(ChatFormatting.BLUE);

		// Operation: try to replace a target that is not present.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: no formatted text is produced.
		Assertions.assertEquals("", result);
	}

	@Test
	public void testReplaceFirstWithLegacyFormatting() {
		// Setup: the target text uses legacy formatting codes.
		Component text = Component.literal(ChatFormatting.BLUE + TARGET);

		// Operation: replace the target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: the legacy color code is preserved before the replacement.
		Assertions.assertEquals(ChatFormatting.BLUE + REPLACEMENT, result);
	}

	@Test
	public void testGetChatFormattingFromTextColorWithLegacyColor() {
		// Setup: a TextColor was created from a legacy formatting color.
		TextColor color = TextColor.fromLegacyFormat(ChatFormatting.BLUE);
		Assertions.assertNotNull(color);

		// Operation: convert the TextColor back to a ChatFormatting value.
		ChatFormatting result = StyledTextHelper.getChatFormattingFromTextColor(color);

		// Assertions: the matching legacy color is returned.
		Assertions.assertEquals(ChatFormatting.BLUE, result);
	}

	@Test
	public void testGetChatFormattingFromTextColorWithNonLegacyColor() {
		// Setup: a TextColor uses an arbitrary RGB value with no legacy equivalent.
		TextColor color = TextColor.fromRgb(0x12_34_56);

		// Operation: try to convert the TextColor back to a ChatFormatting value.
		ChatFormatting result = StyledTextHelper.getChatFormattingFromTextColor(color);

		// Assertions: unsupported colors are not converted.
		Assertions.assertNull(result);
	}

	@Test
	public void testGetStyledTextSegments() {
		// Setup: a component has two styled text segments.
		Component text = Component.empty()
			.append(Component.literal(LABEL).withStyle(ChatFormatting.RED))
			.append(Component.literal(TARGET).withStyle(ChatFormatting.BLUE));

		// Operation: split the component into styled text segments.
		List<StyledTextHelper.StyledTextSegment> result = StyledTextHelper.getStyledTextSegments(text);

		// Assertions: each segment keeps its range, raw text, plain text, and style.
		Assertions.assertEquals(2, result.size());
		StyledTextHelper.StyledTextSegment prefix = result.getFirst();
		Assertions.assertEquals(0, prefix.start());
		Assertions.assertEquals(LABEL.length(), prefix.end());
		Assertions.assertEquals(LABEL, prefix.rawText());
		Assertions.assertEquals(LABEL, prefix.plainText());
		Assertions.assertEquals(ChatFormatting.RED.toString(), StyledTextHelper.getLegacyFormattingFromStyle(prefix.style()));

		StyledTextHelper.StyledTextSegment target = result.get(1);
		Assertions.assertEquals(LABEL.length(), target.start());
		Assertions.assertEquals(LABEL.length() + TARGET.length(), target.end());
		Assertions.assertEquals(TARGET, target.rawText());
		Assertions.assertEquals(TARGET, target.plainText());
		Assertions.assertEquals(ChatFormatting.BLUE.toString(), StyledTextHelper.getLegacyFormattingFromStyle(target.style()));
	}

	@Test
	public void testGetStyledTextSegmentsStripsLegacyFormattingForPlainTextRange() {
		// Setup: a component segment contains legacy formatting codes in its raw text.
		Component text = Component.literal(ChatFormatting.BLUE + TARGET);

		// Operation: split the component into styled text segments.
		List<StyledTextHelper.StyledTextSegment> result = StyledTextHelper.getStyledTextSegments(text);

		// Assertions: raw text keeps legacy codes, while plain text and ranges do not count them.
		StyledTextHelper.StyledTextSegment segment = result.getFirst();
		Assertions.assertEquals(ChatFormatting.BLUE + TARGET, segment.rawText());
		Assertions.assertEquals(TARGET, segment.plainText());
		Assertions.assertEquals(0, segment.start());
		Assertions.assertEquals(TARGET.length(), segment.end());
	}

	@Test
	public void testGetTextRangeFindsTargetAcrossSegments() {
		// Setup: the target text is split across two styled segments.
		List<StyledTextHelper.StyledTextSegment> segments = StyledTextHelper.getStyledTextSegments(
			Component.empty()
				.append(Component.literal(TARGET_START).withStyle(ChatFormatting.BLUE))
				.append(Component.literal(TARGET_END).withStyle(ChatFormatting.BLUE))
		);

		// Operation: find the target text range in the joined plain text.
		Optional<StyledTextHelper.TextRange> result = StyledTextHelper.getTextRange(segments, TARGET);

		// Assertions: the target range covers the joined segment text.
		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(new StyledTextHelper.TextRange(0, TARGET.length()), result.get());
	}

	@Test
	public void testGetTextRangeReturnsEmptyForMissingTarget() {
		// Setup: the segments do not contain the target text.
		List<StyledTextHelper.StyledTextSegment> segments = StyledTextHelper.getStyledTextSegments(
			Component.literal("No matching text")
		);

		// Operation: try to find the missing target text range.
		Optional<StyledTextHelper.TextRange> result = StyledTextHelper.getTextRange(segments, TARGET);

		// Assertions: no range is returned.
		Assertions.assertTrue(result.isEmpty());
	}

	@Test
	public void testTextRangeIntersects() {
		// Setup: a text range covers the middle of a plain-text line.
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(5, 10);

		// Operation: compare the range against before, overlapping, and after ranges.
		boolean before = range.intersects(0, 5);
		boolean overlappingStart = range.intersects(0, 6);
		boolean inside = range.intersects(6, 9);
		boolean overlappingEnd = range.intersects(9, 12);
		boolean after = range.intersects(10, 12);

		// Assertions: only ranges that share at least one character intersect.
		Assertions.assertFalse(before);
		Assertions.assertTrue(overlappingStart);
		Assertions.assertTrue(inside);
		Assertions.assertTrue(overlappingEnd);
		Assertions.assertFalse(after);
	}

	@Test
	public void testStyledTextSegmentIntersects() {
		// Setup: a styled segment covers the middle of a plain-text line.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(5, 10, "abcde", "abcde", Style.EMPTY);

		// Operation: compare the segment against before, overlapping, and after target ranges.
		boolean before = segment.intersects(new StyledTextHelper.TextRange(0, 5));
		boolean overlapping = segment.intersects(new StyledTextHelper.TextRange(4, 6));
		boolean after = segment.intersects(new StyledTextHelper.TextRange(10, 12));

		// Assertions: the segment delegates intersection checks to the range.
		Assertions.assertFalse(before);
		Assertions.assertTrue(overlapping);
		Assertions.assertFalse(after);
	}

	@Test
	public void testReplaceFirstWithComponentStyle() {
		// Setup: the target text uses component style instead of legacy formatting codes.
		Component text = Component.literal(TARGET)
			.withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);

		// Operation: replace the target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: the component style is converted to legacy codes before the replacement.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstWithAllComponentStyleFlags() {
		// Setup: the target text uses every legacy-compatible component style flag.
		Component text = Component.literal(TARGET)
			.withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE, ChatFormatting.STRIKETHROUGH, ChatFormatting.OBFUSCATED);

		// Operation: replace the styled target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: color and all style flags are preserved in stable order.
		String expected = ChatFormatting.GREEN.toString() +
			ChatFormatting.BOLD +
			ChatFormatting.ITALIC +
			ChatFormatting.UNDERLINE +
			ChatFormatting.STRIKETHROUGH +
			ChatFormatting.OBFUSCATED +
			REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstWithNonLegacyTextColor() {
		// Setup: the target text uses an RGB component color that has no legacy color code.
		Component text = Component.literal(TARGET)
			.withColor(0x12_34_56);

		// Operation: replace the styled target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: the unsupported color is omitted, but replacement still occurs.
		Assertions.assertEquals(REPLACEMENT, result);
	}

	@Test
	public void testReplaceFirstWithLegacyAndComponentStyle() {
		// Setup: the target text combines legacy color with direct component style.
		Component text = Component.literal(ChatFormatting.BLUE + TARGET)
			.withStyle(ChatFormatting.ITALIC);

		// Operation: replace the styled target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: both legacy and component styles are preserved before the replacement.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstPreservesStyleOutsideTarget() {
		// Setup: the prefix and target use different component styles.
		Component text = Component.empty()
			.append(Component.literal(LABEL).withStyle(ChatFormatting.RED))
			.append(Component.literal(TARGET).withStyle(ChatFormatting.BLUE));

		// Operation: replace the target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: the prefix and target styles are preserved.
		String expected = ChatFormatting.RED + LABEL + ChatFormatting.BLUE + REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstWithStyleOnPrefixAndTargetSegment() {
		// Setup: part of the prefix is red and the rest of the line, including the target, is blue.
		Component text = Component.empty()
			.append(Component.literal("Pre").withStyle(ChatFormatting.RED))
			.append(Component.literal("fix: " + TARGET).withStyle(ChatFormatting.BLUE));

		// Operation: replace the target when prefix text shares a component with it.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: the prefix and target segment styles are preserved.
		String expected = ChatFormatting.RED + "Pre" + ChatFormatting.BLUE + "fix: " + REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstWithTargetSplitAcrossMatchingComponents() {
		// Setup: the target is split across two components with matching styles.
		Component text = Component.empty()
			.append(Component.literal(TARGET_START).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC))
			.append(Component.literal(TARGET_END).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));

		// Operation: replace the split target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: matching styles across all target parts are applied to the replacement.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstWithTargetSplitAcrossDifferentStyles() {
		// Setup: the target is split across components with different styles.
		Component text = Component.empty()
			.append(Component.literal(TARGET_START).withStyle(ChatFormatting.BLUE))
			.append(Component.literal(TARGET_END).withStyle(ChatFormatting.ITALIC));

		// Operation: replace the split target text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: inconsistent target styling is not collapsed into one replacement style.
		Assertions.assertEquals(REPLACEMENT, result);
	}

	@Test
	public void testReplaceFirstWithSegmentsAndRange() {
		// Setup: styled text segments and an explicit target range identify the target text.
		List<StyledTextHelper.StyledTextSegment> segments = StyledTextHelper.getStyledTextSegments(
			Component.literal(LABEL + TARGET).withStyle(ChatFormatting.BLUE)
		);
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(LABEL.length(), LABEL.length() + TARGET.length());

		// Operation: replace the explicit target range.
		String result = StyledTextHelper.replaceFirst(segments, range, REPLACEMENT);

		// Assertions: surrounding text and target style are preserved.
		String expected = ChatFormatting.BLUE + LABEL + REPLACEMENT;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testAppendSegmentWithReplacementForNonIntersectingSegment() {
		// Setup: the segment is before the target range.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, LABEL.length(), LABEL, LABEL, Style.EMPTY.applyFormat(ChatFormatting.RED));
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(LABEL.length(), LABEL.length() + TARGET.length());
		StringBuilder formattedText = new StringBuilder();

		// Operation: append the segment while replacement has not been added.
		boolean addedReplacement = StyledTextHelper.appendSegmentWithReplacement(formattedText, segment, range, REPLACEMENT, true, false);

		// Assertions: the segment text is appended and the replacement flag is unchanged.
		Assertions.assertFalse(addedReplacement);
		Assertions.assertEquals(ChatFormatting.RED + LABEL, formattedText.toString());
	}

	@Test
	public void testAppendSegmentWithReplacementForIntersectingSegment() {
		// Setup: the segment contains the target range.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, TARGET.length(), TARGET, TARGET, Style.EMPTY.applyFormat(ChatFormatting.BLUE));
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(0, TARGET.length());
		StringBuilder formattedText = new StringBuilder();

		// Operation: append the segment and replacement.
		boolean addedReplacement = StyledTextHelper.appendSegmentWithReplacement(formattedText, segment, range, REPLACEMENT, true, false);

		// Assertions: the replacement is appended with segment style and the replacement flag is set.
		Assertions.assertTrue(addedReplacement);
		Assertions.assertEquals(ChatFormatting.BLUE + REPLACEMENT, formattedText.toString());
	}

	@Test
	public void testAppendSegmentWithReplacementDoesNotAddReplacementTwice() {
		// Setup: the segment intersects a range whose replacement was already added.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(TARGET_START.length(), TARGET.length(), TARGET_END, TARGET_END, Style.EMPTY.applyFormat(ChatFormatting.BLUE));
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(0, TARGET.length());
		StringBuilder formattedText = new StringBuilder(REPLACEMENT);

		// Operation: append the second intersecting segment after replacement has already been added.
		boolean addedReplacement = StyledTextHelper.appendSegmentWithReplacement(formattedText, segment, range, REPLACEMENT, true, true);

		// Assertions: no duplicate replacement is appended.
		Assertions.assertTrue(addedReplacement);
		Assertions.assertEquals(REPLACEMENT, formattedText.toString());
	}

	@Test
	public void testFormatReplacementWithConsistentStyle() {
		// Setup: a styled segment contains the whole target range.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, TARGET.length(), TARGET, TARGET, Style.EMPTY.applyFormat(ChatFormatting.BLUE));
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(0, TARGET.length());

		// Operation: format a replacement for a consistently styled target.
		String result = StyledTextHelper.formatReplacement(segment, range, REPLACEMENT, true);

		// Assertions: the replacement keeps the segment style.
		Assertions.assertEquals(ChatFormatting.BLUE + REPLACEMENT, result);
	}

	@Test
	public void testFormatReplacementWithoutConsistentStyle() {
		// Setup: a styled segment contains part of an inconsistently styled target range.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, TARGET_START.length(), TARGET_START, TARGET_START, Style.EMPTY.applyFormat(ChatFormatting.BLUE));
		StyledTextHelper.TextRange range = new StyledTextHelper.TextRange(0, TARGET.length());

		// Operation: format a replacement for an inconsistently styled target.
		String result = StyledTextHelper.formatReplacement(segment, range, REPLACEMENT, false);

		// Assertions: no segment style is applied to the replacement.
		Assertions.assertEquals(REPLACEMENT, result);
	}

	@Test
	public void testIsTargetStyleConsistentWithMatchingStyles() {
		// Setup: the target is split across components with matching styles.
		List<StyledTextHelper.StyledTextSegment> segments = StyledTextHelper.getStyledTextSegments(
			Component.empty()
				.append(Component.literal(TARGET_START).withStyle(ChatFormatting.BLUE))
				.append(Component.literal(TARGET_END).withStyle(ChatFormatting.BLUE))
		);

		// Operation: check whether the target range has one consistent style.
		boolean result = StyledTextHelper.isTargetStyleConsistent(segments, new StyledTextHelper.TextRange(0, TARGET.length()));

		// Assertions: matching styles are considered consistent.
		Assertions.assertTrue(result);
	}

	@Test
	public void testIsTargetStyleConsistentWithDifferentStyles() {
		// Setup: the target is split across components with different styles.
		List<StyledTextHelper.StyledTextSegment> segments = StyledTextHelper.getStyledTextSegments(
			Component.empty()
				.append(Component.literal(TARGET_START).withStyle(ChatFormatting.BLUE))
				.append(Component.literal(TARGET_END).withStyle(ChatFormatting.ITALIC))
		);

		// Operation: check whether the target range has one consistent style.
		boolean result = StyledTextHelper.isTargetStyleConsistent(segments, new StyledTextHelper.TextRange(0, TARGET.length()));

		// Assertions: different styles are not considered consistent.
		Assertions.assertFalse(result);
	}

	@Test
	public void testFormatSegmentText() {
		// Setup: a styled segment has a prefix and target text.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, LABEL.length() + TARGET.length(), LABEL + TARGET, LABEL + TARGET, Style.EMPTY.applyFormat(ChatFormatting.BLUE));

		// Operation: format only the prefix range from the segment.
		String result = StyledTextHelper.formatSegmentText(segment, 0, LABEL.length());

		// Assertions: the segment style is applied to the selected text.
		Assertions.assertEquals(ChatFormatting.BLUE + LABEL, result);
	}

	@Test
	public void testFormatSegmentReplacementWithIncludedStyle() {
		// Setup: a styled segment contains the target text.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, TARGET.length(), TARGET, TARGET, Style.EMPTY.applyFormat(ChatFormatting.BLUE));

		// Operation: format a replacement and include the segment style.
		String result = StyledTextHelper.formatSegmentReplacement(segment, 0, TARGET.length(), REPLACEMENT, true);

		// Assertions: the segment style is applied to the replacement.
		Assertions.assertEquals(ChatFormatting.BLUE + REPLACEMENT, result);
	}

	@Test
	public void testFormatSegmentReplacementWithoutIncludedStyle() {
		// Setup: a styled segment contains the target text.
		StyledTextHelper.StyledTextSegment segment = new StyledTextHelper.StyledTextSegment(0, TARGET.length(), TARGET, TARGET, Style.EMPTY.applyFormat(ChatFormatting.BLUE));

		// Operation: format a replacement without including the segment style.
		String result = StyledTextHelper.formatSegmentReplacement(segment, 0, TARGET.length(), REPLACEMENT, false);

		// Assertions: the replacement is returned without segment style.
		Assertions.assertEquals(REPLACEMENT, result);
	}

	@Test
	public void testReplaceFirstWithStyledSuffixAfterTarget() {
		// Setup: the target component has styled suffix text after the target.
		Component text = Component.literal(TARGET + " suffix")
			.withStyle(ChatFormatting.BLUE);

		// Operation: replace the target while preserving suffix text.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: the styled suffix is preserved after the replacement.
		String expected = ChatFormatting.BLUE + REPLACEMENT + ChatFormatting.BLUE + " suffix";
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testReplaceFirstOnlyReplacesFirstTarget() {
		// Setup: the same target appears twice with one component style.
		Component text = Component.literal(TARGET + " " + TARGET)
			.withStyle(ChatFormatting.BLUE);

		// Operation: replace the first target.
		String result = StyledTextHelper.replaceFirst(text, TARGET, REPLACEMENT);

		// Assertions: only the first target is replaced and the second target is preserved.
		String expected = ChatFormatting.BLUE + REPLACEMENT + ChatFormatting.BLUE + " " + TARGET;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testGetRawTextRangePreservesActiveLegacyFormatting() {
		// Setup: legacy formatting changes before the requested plain-text range.
		String rawText = ChatFormatting.RED + LABEL + ChatFormatting.BLUE + TARGET;

		// Operation: slice out the target text from the raw legacy-formatted text.
		String result = StyledTextHelper.getRawTextRange(rawText, LABEL.length(), LABEL.length() + TARGET.length());

		// Assertions: active legacy formatting is carried into the sliced range.
		String expected = ChatFormatting.RED.toString() + ChatFormatting.BLUE + TARGET;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testGetRawTextRangePreservesFormattingInsideRange() {
		// Setup: legacy formatting changes inside the requested plain-text range.
		String rawText = LABEL + ChatFormatting.BLUE + TARGET;

		// Operation: slice a range containing both plain text and a legacy formatting change.
		String result = StyledTextHelper.getRawTextRange(rawText, 0, LABEL.length() + TARGET.length());

		// Assertions: formatting inside the range is preserved in place.
		String expected = LABEL + ChatFormatting.BLUE + TARGET;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testApplyStyleToTextInsertsComponentStyleAfterExistingLegacyCodes() {
		// Setup: text already starts with a legacy color code and the component style adds italic.
		String text = ChatFormatting.BLUE + TARGET;
		Style style = Style.EMPTY.applyFormat(ChatFormatting.ITALIC);

		// Operation: apply the component style to the legacy-formatted text.
		String result = StyledTextHelper.applyStyleToText(style, text);

		// Assertions: the component style is inserted after existing leading legacy codes.
		String expected = ChatFormatting.BLUE.toString() + ChatFormatting.ITALIC + TARGET;
		Assertions.assertEquals(expected, result);
	}

	@Test
	public void testGetLegacyFormattingFromStyleWithEmptyStyle() {
		// Setup: the style has no legacy-compatible formatting.
		Style style = Style.EMPTY;

		// Operation: convert the empty style to legacy formatting.
		String result = StyledTextHelper.getLegacyFormattingFromStyle(style);

		// Assertions: no formatting codes are produced.
		Assertions.assertEquals("", result);
	}
}
