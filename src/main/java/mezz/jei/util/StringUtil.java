package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

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

	public static Component truncateStringToWidth(Component text, int width, Font fontRenderer) {
		int ellipsisWidth = fontRenderer.width("...");
		FormattedText truncatedText = fontRenderer.substrByWidth(text, width - ellipsisWidth);
		String truncatedTextString = truncatedText.getString();
		return new TextComponent(truncatedTextString + "...");
	}

	public static List<FormattedText> splitLines(FormattedText line, int width) {
		if (width <= 0) {
			return List.of(line);
		}

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		StringSplitter splitter = font.getSplitter();
		return splitter.splitLines(line, width, Style.EMPTY);
	}

	public static List<FormattedText> splitLines(List<? extends FormattedText> lines, int width) {
		if (width <= 0) {
			return List.copyOf(lines);
		}

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		StringSplitter splitter = font.getSplitter();
		return lines.stream()
			.flatMap(text -> splitter.splitLines(text, width, Style.EMPTY).stream())
			.toList();
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
}
