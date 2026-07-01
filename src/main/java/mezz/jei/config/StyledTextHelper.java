package mezz.jei.config;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StyledTextHelper {
	private static final char FORMATTING_PREFIX = '\u00A7';

	private StyledTextHelper() {
	}

	public static String replaceFirst(ITextComponent text, String target, String replacement) {
		List<StyledTextSegment> segments = getStyledTextSegments(text);
		return getTextRange(segments, target)
			.map(textRange -> replaceFirst(segments, textRange, replacement))
			.orElse("");
	}

	public static String getLegacyFormattingFromStyle(Style style) {
		StringBuilder formatting = new StringBuilder();
		Color color = style.getColor();
		if (color != null) {
			TextFormatting colorFormatting = getTextFormattingFromColor(color);
			if (colorFormatting != null) {
				formatting.append(colorFormatting);
			}
		}
		if (style.isBold()) {
			formatting.append(TextFormatting.BOLD);
		}
		if (style.isItalic()) {
			formatting.append(TextFormatting.ITALIC);
		}
		if (style.isUnderlined()) {
			formatting.append(TextFormatting.UNDERLINE);
		}
		if (style.isStrikethrough()) {
			formatting.append(TextFormatting.STRIKETHROUGH);
		}
		if (style.isObfuscated()) {
			formatting.append(TextFormatting.OBFUSCATED);
		}
		return formatting.toString();
	}

	@Nullable
	private static TextFormatting getTextFormattingFromColor(Color color) {
		for (TextFormatting formatting : TextFormatting.values()) {
			if (formatting.isColor() && color.equals(Color.fromLegacyFormat(formatting))) {
				return formatting;
			}
		}
		return null;
	}

	private static List<StyledTextSegment> getStyledTextSegments(ITextComponent text) {
		List<StyledTextSegment> segments = new ArrayList<>();
		int[] textLength = {0};
		text.visit((style, rawText) -> {
			String plainText = TextFormatting.stripFormatting(rawText);
			if (plainText != null && !plainText.isEmpty()) {
				int start = textLength[0];
				textLength[0] += plainText.length();
				segments.add(new StyledTextSegment(start, textLength[0], rawText, plainText, style));
			}
			return Optional.empty();
		}, Style.EMPTY);
		return segments;
	}

	private static Optional<TextRange> getTextRange(List<StyledTextSegment> segments, String target) {
		String text = segments.stream()
			.map(StyledTextSegment::getPlainText)
			.collect(Collectors.joining());
		int targetStart = text.indexOf(target);
		if (targetStart < 0) {
			return Optional.empty();
		}
		return Optional.of(new TextRange(targetStart, targetStart + target.length()));
	}

	private static String replaceFirst(List<StyledTextSegment> segments, TextRange targetRange, String replacement) {
		boolean targetStyleConsistent = isTargetStyleConsistent(segments, targetRange);
		StringBuilder formattedText = new StringBuilder();
		boolean addedReplacement = false;
		for (StyledTextSegment segment : segments) {
			addedReplacement = appendSegmentWithReplacement(
				formattedText,
				segment,
				targetRange,
				replacement,
				targetStyleConsistent,
				addedReplacement
			);
		}
		return formattedText.toString();
	}

	private static boolean appendSegmentWithReplacement(
		StringBuilder formattedText,
		StyledTextSegment segment,
		TextRange targetRange,
		String replacement,
		boolean targetStyleConsistent,
		boolean addedReplacement
	) {
		if (!segment.intersects(targetRange)) {
			formattedText.append(formatSegmentText(segment, segment.getStart(), segment.getEnd()));
			return addedReplacement;
		}
		if (segment.getStart() < targetRange.getStart()) {
			formattedText.append(formatSegmentText(segment, segment.getStart(), targetRange.getStart()));
		}
		if (!addedReplacement) {
			formattedText.append(formatReplacement(segment, targetRange, replacement, targetStyleConsistent));
			addedReplacement = true;
		}
		if (segment.getEnd() > targetRange.getEnd()) {
			formattedText.append(formatSegmentText(segment, targetRange.getEnd(), segment.getEnd()));
		}
		return addedReplacement;
	}

	private static String formatReplacement(StyledTextSegment segment, TextRange targetRange, String replacement, boolean targetStyleConsistent) {
		if (!targetStyleConsistent) {
			return replacement;
		}
		int overlapStart = Math.max(segment.getStart(), targetRange.getStart());
		int overlapEnd = Math.min(segment.getEnd(), targetRange.getEnd());
		boolean includeStyle = segment.getStart() >= targetRange.getStart();
		return formatSegmentReplacement(segment, overlapStart, overlapEnd, replacement, includeStyle);
	}

	private static boolean isTargetStyleConsistent(List<StyledTextSegment> segments, TextRange targetRange) {
		Style targetStyle = null;
		for (StyledTextSegment segment : segments) {
			if (!segment.intersects(targetRange)) {
				continue;
			}
			Style segmentStyle = segment.getStyle();
			if (targetStyle == null) {
				targetStyle = segmentStyle;
			} else if (!targetStyle.equals(segmentStyle)) {
				return false;
			}
		}
		return targetStyle != null;
	}

	private static String formatSegmentText(StyledTextSegment segment, int start, int end) {
		String rawText = getRawTextRange(segment.getRawText(), start - segment.getStart(), end - segment.getStart());
		return applyStyleToText(segment.getStyle(), rawText);
	}

	private static String formatSegmentReplacement(StyledTextSegment segment, int start, int end, String replacement, boolean includeStyle) {
		String rawText = getRawTextRange(segment.getRawText(), start - segment.getStart(), end - segment.getStart());
		if (includeStyle) {
			rawText = applyStyleToText(segment.getStyle(), rawText);
		}
		String plainText = TextFormatting.stripFormatting(rawText);
		if (plainText == null || plainText.isEmpty()) {
			return replacement;
		}
		return StringUtils.replaceOnce(rawText, plainText, replacement);
	}

	private static String applyStyleToText(Style style, String text) {
		String formatting = getLegacyFormattingFromStyle(style);
		if (formatting.isEmpty() || text.isEmpty()) {
			return text;
		}
		int index = 0;
		while (index + 1 < text.length() && text.charAt(index) == FORMATTING_PREFIX) {
			index += 2;
		}
		return text.substring(0, index) + formatting + text.substring(index);
	}

	private static String getRawTextRange(String rawText, int start, int end) {
		StringBuilder rawTextRange = new StringBuilder();
		StringBuilder activeFormatting = new StringBuilder();
		boolean addedActiveFormatting = false;
		int plainIndex = 0;
		for (int i = 0; i < rawText.length(); i++) {
			char c = rawText.charAt(i);
			if (c == FORMATTING_PREFIX && i + 1 < rawText.length()) {
				String formatting = rawText.substring(i, i + 2);
				if (plainIndex < start) {
					activeFormatting.append(formatting);
				} else if (plainIndex < end) {
					rawTextRange.append(formatting);
				}
				i++;
				continue;
			}
			if (plainIndex >= start && plainIndex < end) {
				if (!addedActiveFormatting) {
					rawTextRange.insert(0, activeFormatting);
					addedActiveFormatting = true;
				}
				rawTextRange.append(c);
			}
			plainIndex++;
		}
		return rawTextRange.toString();
	}

	private static final class StyledTextSegment {
		private final int start;
		private final int end;
		private final String rawText;
		private final String plainText;
		private final Style style;

		private StyledTextSegment(int start, int end, String rawText, String plainText, Style style) {
			this.start = start;
			this.end = end;
			this.rawText = rawText;
			this.plainText = plainText;
			this.style = style;
		}

		private int getStart() {
			return start;
		}

		private int getEnd() {
			return end;
		}

		private String getRawText() {
			return rawText;
		}

		private String getPlainText() {
			return plainText;
		}

		private Style getStyle() {
			return style;
		}

		private boolean intersects(TextRange range) {
			return range.intersects(start, end);
		}
	}

	private static final class TextRange {
		private final int start;
		private final int end;

		private TextRange(int start, int end) {
			this.start = start;
			this.end = end;
		}

		private int getStart() {
			return start;
		}

		private int getEnd() {
			return end;
		}

		private boolean intersects(int start, int end) {
			return this.start < end && this.end > start;
		}
	}
}
