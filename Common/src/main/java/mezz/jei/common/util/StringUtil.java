package mezz.jei.common.util;

import mezz.jei.core.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
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
		String result = ChatFormatting.stripFormatting(string);
		return result == null ? "" : result;
	}

	public static FormattedText truncateStringToWidth(FormattedText text, int width, Font font) {
		int ellipsisWidth = font.width("...");
		StringSplitter splitter = font.getSplitter();

		FormattedText truncatedText = font.substrByWidth(text, width - ellipsisWidth);

		Style style = splitter.componentStyleAtWidth(text, width - ellipsisWidth);
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

		StringSplitter splitter = font.getSplitter();
		List<FormattedText> result = new ArrayList<>();
		for (FormattedText line : lines) {
			List<FormattedText> splitLines;
			if (line.getString().isEmpty()) {
				splitLines = List.of(line);
			} else {
				splitLines = splitter.splitLines(line, width, Style.EMPTY);
			}

			for (FormattedText splitLine : splitLines) {
				if (result.size() == maxLines) {
					// result is at the max size, but we still have more to add.
					// Truncate the last line to indicate that there is more text that can't be displayed.
					FormattedText last = result.remove(result.size() - 1);
					last = truncateStringToWidth(last, width, font);
					result.add(last);
					return new Pair<>(result, true);
				}
				result.add(splitLine);
			}

		}

		return new Pair<>(result, false);
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

	public static void drawCenteredStringWithShadow(GuiGraphics guiGraphics, Font font, String string, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, string);
		guiGraphics.drawString(font, string, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	public static void drawCenteredStringWithShadow(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text);
		guiGraphics.drawString(font, text, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}
}
