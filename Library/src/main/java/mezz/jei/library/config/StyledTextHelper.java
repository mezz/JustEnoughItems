package mezz.jei.library.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StyledTextHelper {
	private StyledTextHelper() {

	}

	public static String replaceFirst(Component text, String target, String replacement) {
		List<StyledTextSegment> segments = getStyledTextSegments(text);
		return getTextRange(segments, target)
			.map(textRange -> replaceFirst(segments, textRange, replacement))
			.orElse("");
	}

	/*
	 * Converts styles added directly to a Component back into legacy formatting codes.
	 * Component#getString() does not preserve these styles, so this is needed when another
	 * mod adds styled text without legacy formatting codes.
	 */
	public static String getLegacyFormattingFromStyle(Style style) {
		StringBuilder formatting = new StringBuilder();

		TextColor color = style.getColor();
		if (color != null) {
			ChatFormatting colorFormatting = getChatFormattingFromTextColor(color);
			if (colorFormatting != null) {
				formatting.append(colorFormatting);
			}
		}

		if (style.isBold()) {
			formatting.append(ChatFormatting.BOLD);
		}
		if (style.isItalic()) {
			formatting.append(ChatFormatting.ITALIC);
		}
		if (style.isUnderlined()) {
			formatting.append(ChatFormatting.UNDERLINE);
		}
		if (style.isStrikethrough()) {
			formatting.append(ChatFormatting.STRIKETHROUGH);
		}
		if (style.isObfuscated()) {
			formatting.append(ChatFormatting.OBFUSCATED);
		}

		return formatting.toString();
	}

	public static @Nullable ChatFormatting getChatFormattingFromTextColor(TextColor color) {
		for (ChatFormatting chatFormatting : ChatFormatting.values()) {
			if (chatFormatting.isColor()) {
				TextColor textColor = TextColor.fromLegacyFormat(chatFormatting);
				if (textColor != null && textColor.equals(color)) {
					return chatFormatting;
				}
			}
		}
		return null;
	}

	public static List<StyledTextSegment> getStyledTextSegments(Component text) {
		List<StyledTextSegment> segments = new ArrayList<>();
		int[] textLength = {0};
		text.visit((style, rawText) -> {
			String plainText = ChatFormatting.stripFormatting(rawText);
			if (!plainText.isEmpty()) {
				int start = textLength[0];
				textLength[0] += plainText.length();
				segments.add(new StyledTextSegment(start, textLength[0], rawText, plainText, style));
			}
			return Optional.empty();
		}, Style.EMPTY);
		return segments;
	}

	public static Optional<TextRange> getTextRange(List<StyledTextSegment> segments, String target) {
		String text = segments.stream()
			.map(StyledTextSegment::plainText)
			.collect(Collectors.joining());
		int targetStart = text.indexOf(target);
		if (targetStart < 0) {
			return Optional.empty();
		}
		return Optional.of(new TextRange(targetStart, targetStart + target.length()));
	}

	public static String replaceFirst(List<StyledTextSegment> segments, TextRange targetRange, String replacement) {
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

	public static boolean appendSegmentWithReplacement(
		StringBuilder formattedText,
		StyledTextSegment segment,
		TextRange targetRange,
		String replacement,
		boolean targetStyleConsistent,
		boolean addedReplacement
	) {
		if (!segment.intersects(targetRange)) {
			formattedText.append(formatSegmentText(segment, segment.start(), segment.end()));
			return addedReplacement;
		}

		if (segment.start() < targetRange.start()) {
			formattedText.append(formatSegmentText(segment, segment.start(), targetRange.start()));
		}

		if (!addedReplacement) {
			formattedText.append(formatReplacement(segment, targetRange, replacement, targetStyleConsistent));
			addedReplacement = true;
		}

		if (segment.end() > targetRange.end()) {
			formattedText.append(formatSegmentText(segment, targetRange.end(), segment.end()));
		}

		return addedReplacement;
	}

	public static String formatReplacement(StyledTextSegment segment, TextRange targetRange, String replacement, boolean targetStyleConsistent) {
		if (!targetStyleConsistent) {
			return replacement;
		}

		int overlapStart = Math.max(segment.start(), targetRange.start());
		int overlapEnd = Math.min(segment.end(), targetRange.end());
		boolean includeStyle = segment.start() >= targetRange.start();
		return formatSegmentReplacement(segment, overlapStart, overlapEnd, replacement, includeStyle);
	}

	public static boolean isTargetStyleConsistent(List<StyledTextSegment> segments, TextRange targetRange) {
		@Nullable
		Style targetStyle = null;
		for (StyledTextSegment segment : segments) {
			if (!segment.intersects(targetRange)) {
				continue;
			}

			Style segmentStyle = segment.style();
			if (targetStyle == null) {
				targetStyle = segmentStyle;
			} else if (!targetStyle.equals(segmentStyle)) {
				return false;
			}
		}
		return targetStyle != null;
	}

	public static String formatSegmentText(StyledTextSegment segment, int start, int end) {
		String rawText = getRawTextRange(segment.rawText(), start - segment.start(), end - segment.start());
		return applyStyleToText(segment.style(), rawText);
	}

	public static String formatSegmentReplacement(StyledTextSegment segment, int start, int end, String replacement, boolean includeStyle) {
		String rawText = getRawTextRange(segment.rawText(), start - segment.start(), end - segment.start());
		if (includeStyle) {
			rawText = applyStyleToText(segment.style(), rawText);
		}
		String plainText = ChatFormatting.stripFormatting(rawText);
		if (plainText.isEmpty()) {
			return replacement;
		}
		return StringUtils.replaceOnce(rawText, plainText, replacement);
	}

	public static String applyStyleToText(Style style, String text) {
		String formatting = getLegacyFormattingFromStyle(style);
		if (formatting.isEmpty() || text.isEmpty()) {
			return text;
		}

		int index = 0;
		while (index + 1 < text.length() && text.charAt(index) == ChatFormatting.PREFIX_CODE) {
			index += 2;
		}
		return text.substring(0, index) + formatting + text.substring(index);
	}

	public static String getRawTextRange(String rawText, int start, int end) {
		StringBuilder rawTextRange = new StringBuilder();
		StringBuilder activeFormatting = new StringBuilder();
		boolean addedActiveFormatting = false;
		int plainIndex = 0;

		for (int i = 0; i < rawText.length(); i++) {
			char c = rawText.charAt(i);
			if (c == ChatFormatting.PREFIX_CODE && i + 1 < rawText.length()) {
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

	public record StyledTextSegment(int start, int end, String rawText, String plainText, Style style) {
		public boolean intersects(TextRange range) {
			return range.intersects(this.start, this.end);
		}
	}

	public record TextRange(int start, int end) {
		public boolean intersects(int start, int end) {
			return this.start < end && this.end > start;
		}
	}
}
