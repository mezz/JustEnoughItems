package mezz.jei.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StringUtil {
	private StringUtil() {

	}

	public static Component stripStyling(Component textComponent) {
		MutableComponent text = textComponent.plainCopy();
		for (Component sibling : textComponent.getSiblings()) {
			text.append(stripStyling(sibling));
		}
		return text;
	}

	public static String removeChatFormatting(String string) {
		return ChatFormatting.stripFormatting(string);
	}

	public static FormattedText truncateStringToWidth(FormattedText text, int width, Font font) {
		if (width <= 0) {
			return FormattedText.EMPTY;
		}
		int ellipsisWidth = font.width("...");

		if (width <= ellipsisWidth) {
			return font.substrByWidth(Component.literal("..."), width);
		}

		FormattedText truncatedText = font.substrByWidth(text, width - ellipsisWidth);

		Style style = font.getSplitter().componentStyleAtWidth(text, width - ellipsisWidth);
		if (style == null) {
			style = Style.EMPTY;
		}

		return FormattedText.composite(truncatedText, Component.literal("...").setStyle(style));
	}

	/**
	 * Split and wrap lines, and truncate the last line if it exceeds the max lines.
	 * @return the wrapped lines, and a boolean indicating if the last line was truncated.
	 */
	public static Pair<List<FormattedText>, Boolean> splitLines(Font font, List<FormattedText> lines, int width, int maxLines) {
		if (lines.isEmpty()) {
			return new Pair<>(List.of(), false);
		}
		if (maxLines <= 0) {
			return new Pair<>(List.of(), true);
		}
		if (width <= 0) {
			return new Pair<>(List.copyOf(lines), false);
		}

		List<FormattedText> result = new ArrayList<>();
		for (FormattedText line : lines) {
			StyledLine styledLine = StyledLine.from(line);
			List<StyledLine> wrappedLines = splitLineAtSpaces(font, styledLine, width);
			for (StyledLine wrappedLine : wrappedLines) {
				HyphenatedLines hyphenatedLines = splitLineWithHyphenation(font, wrappedLine, width);
				for (FormattedText splitLine : hyphenatedLines.lines()) {
					if (!addLine(result, splitLine, width, maxLines, font)) {
						return new Pair<>(result, true);
					}
				}
				if (hyphenatedLines.truncated()) {
					return new Pair<>(result, true);
				}
			}

		}

		return new Pair<>(result, false);
	}

	private static boolean addLine(List<FormattedText> result, FormattedText line, int width, int maxLines, Font font) {
		if (result.size() == maxLines) {
			FormattedText last = result.removeLast();
			last = truncateStringToWidth(last, width, font);
			result.add(last);
			return false;
		}
		result.add(line);
		return true;
	}

	private static List<StyledLine> splitLineAtSpaces(Font font, StyledLine line, int width) {
		String text = line.getString();
		if (text.isEmpty()) {
			return List.of(line);
		}

		List<StyledLine> result = new ArrayList<>();
		int lineStart = 0;
		int lastBreak = -1;
		int i = 0;
		while (i < text.length()) {
			int codePoint = text.codePointAt(i);
			int next = i + Character.charCount(codePoint);
			if (codePoint == '\n') {
				addRange(result, line, lineStart, i);
				lineStart = next;
				lastBreak = -1;
				i = next;
				continue;
			}

			if (isWrappableWhitespace(codePoint)) {
				lastBreak = i;
			}

			if (lastBreak >= lineStart && font.width(line.substring(lineStart, next).asFormattedText()) > width) {
				addRange(result, line, lineStart, trimTrailingWhitespace(text, lineStart, lastBreak));
				lineStart = skipWrappableWhitespace(text, lastBreak + 1);
				lastBreak = -1;
				i = lineStart;
				continue;
			}

			i = next;
		}

		addRange(result, line, lineStart, text.length());
		return result;
	}

	private static void addRange(List<StyledLine> result, StyledLine line, int start, int end) {
		if (start <= line.getString().length()) {
			result.add(line.substring(start, Math.max(start, end)));
		}
	}

	private static int trimTrailingWhitespace(String text, int start, int end) {
		while (end > start) {
			int previous = text.offsetByCodePoints(end, -1);
			int codePoint = text.codePointAt(previous);
			if (!isWrappableWhitespace(codePoint)) {
				break;
			}
			end = previous;
		}
		return end;
	}

	private static int skipWrappableWhitespace(String text, int start) {
		while (start < text.length()) {
			int codePoint = text.codePointAt(start);
			if (!isWrappableWhitespace(codePoint)) {
				break;
			}
			start += Character.charCount(codePoint);
		}
		return start;
	}

	private static boolean isWrappableWhitespace(int codePoint) {
		return codePoint != '\n' && Character.isWhitespace(codePoint);
	}

	private static HyphenatedLines splitLineWithHyphenation(Font font, StyledLine line, int width) {
		FormattedText formattedLine = line.asFormattedText();
		if (line.getString().isEmpty() || font.width(formattedLine) <= width) {
			return new HyphenatedLines(List.of(formattedLine), false);
		}

		int hyphenWidth = font.width("-");
		if (width <= hyphenWidth) {
			return new HyphenatedLines(List.of(truncateStringToWidth(formattedLine, width, font)), true);
		}

		List<FormattedText> result = new ArrayList<>();
		String text = line.getString();
		int start = 0;
		while (start < text.length()) {
			StyledLine remaining = line.substring(start, text.length());
			FormattedText remainingText = remaining.asFormattedText();
			if (font.width(remainingText) <= width) {
				result.add(remainingText);
				return new HyphenatedLines(result, false);
			}

			int splitEnd = findHyphenSplitEnd(font, line, start, text.length(), width - hyphenWidth);
			if (splitEnd <= start) {
				result.add(truncateStringToWidth(remainingText, width, font));
				return new HyphenatedLines(result, true);
			}

			Style hyphenStyle = line.getStyleAt(text.offsetByCodePoints(splitEnd, -1));
			result.add(FormattedText.composite(
				line.substring(start, splitEnd).asFormattedText(),
				FormattedText.of("-", hyphenStyle)
			));
			start = splitEnd;
		}

		return new HyphenatedLines(result, false);
	}

	private static int findHyphenSplitEnd(Font font, StyledLine line, int start, int end, int width) {
		int splitEnd = start;
		int i = start;
		while (i < end) {
			int next = i + Character.charCount(line.getString().codePointAt(i));
			if (font.width(line.substring(start, next).asFormattedText()) > width) {
				break;
			}
			splitEnd = next;
			i = next;
		}
		return splitEnd;
	}

	private record HyphenatedLines(List<FormattedText> lines, boolean truncated) {
	}

	private record StyledPart(String text, Style style) {
	}

	private static final class StyledLine {
		private final List<StyledPart> parts;
		private final String string;

		private StyledLine(List<StyledPart> parts) {
			this.parts = List.copyOf(parts);
			this.string = parts.stream()
				.map(StyledPart::text)
				.collect(Collectors.joining());
		}

		public static StyledLine from(FormattedText text) {
			List<StyledPart> parts = new ArrayList<>();
			text.visit((style, contents) -> {
				if (!contents.isEmpty()) {
					parts.add(new StyledPart(contents, style));
				}
				return Optional.empty();
			}, Style.EMPTY);
			return new StyledLine(parts);
		}

		public String getString() {
			return string;
		}

		public StyledLine substring(int start, int end) {
			if (start == end) {
				return new StyledLine(List.of());
			}

			List<StyledPart> result = new ArrayList<>();
			int offset = 0;
			for (StyledPart part : parts) {
				int partStart = offset;
				int partEnd = partStart + part.text().length();
				if (partEnd > start && partStart < end) {
					int substringStart = Math.max(start, partStart) - partStart;
					int substringEnd = Math.min(end, partEnd) - partStart;
					result.add(new StyledPart(part.text().substring(substringStart, substringEnd), part.style()));
				}
				offset = partEnd;
			}
			return new StyledLine(result);
		}

		public Style getStyleAt(int index) {
			int offset = 0;
			for (StyledPart part : parts) {
				int partEnd = offset + part.text().length();
				if (index < partEnd) {
					return part.style();
				}
				offset = partEnd;
			}
			return Style.EMPTY;
		}

		public FormattedText asFormattedText() {
			if (parts.isEmpty()) {
				return FormattedText.EMPTY;
			}
			return FormattedText.composite(
				parts.stream()
					.map(part -> FormattedText.of(part.text(), part.style()))
					.toList()
			);
		}
	}

	public static List<FormattedText> expandNewlines(Component... descriptionComponents) {
		List<FormattedText> descriptionLinesExpanded = new ArrayList<>();
		for (Component descriptionLine : descriptionComponents) {
			ExpandNewLineTextAcceptor newLineTextAcceptor = new ExpandNewLineTextAcceptor();
			descriptionLine.visit(newLineTextAcceptor, Style.EMPTY);
			newLineTextAcceptor.addLinesTo(descriptionLinesExpanded);
		}
		return descriptionLinesExpanded;
	}

	public static String intsToString(Collection<Integer> indexes) {
		return indexes.stream()
			.sorted()
			.map(i -> Integer.toString(i))
			.collect(Collectors.joining(", "));
	}

	public static void drawCenteredStringWithShadow(GuiGraphics guiGraphics, Font font, String string, ImmutableRect2i area, int color) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, string);
		guiGraphics.drawString(font, string, textArea.getX(), textArea.getY(), color);
	}

	public static void drawCenteredStringWithShadow(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, ImmutableRect2i area, int color) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text);
		guiGraphics.drawString(font, text, textArea.getX(), textArea.getY(), color);
	}
}
