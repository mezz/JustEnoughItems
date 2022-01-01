package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.CharacterManager;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class StringUtil {
	private StringUtil() {

	}

	public static ITextComponent stripStyling(ITextComponent textComponent) {
		IFormattableTextComponent text = textComponent.plainCopy();
		for (ITextComponent sibling : textComponent.getSiblings()) {
			text.append(stripStyling(sibling));
		}
		return text;
	}

	public static ITextComponent truncateStringToWidth(ITextComponent text, int width, FontRenderer fontRenderer) {
		int ellipsisWidth = fontRenderer.width("...");
		ITextProperties truncatedText = fontRenderer.substrByWidth(text, width - ellipsisWidth);
		String truncatedTextString = truncatedText.getString();
		return new StringTextComponent(truncatedTextString + "...");
	}

	public static List<ITextProperties> splitLines(ITextProperties line, int width) {
		if (width <= 0) {
			return Collections.singletonList(line);
		}

		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		CharacterManager splitter = font.getSplitter();
		return splitter.splitLines(line, width, Style.EMPTY);
	}

	public static List<ITextProperties> splitLines(List<? extends ITextProperties> lines, int width) {
		if (width <= 0) {
			//noinspection unchecked
			return (List<ITextProperties>) lines;
		}

		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = minecraft.font;
		CharacterManager splitter = font.getSplitter();
		return lines.stream()
			.flatMap(text -> splitter.splitLines(text, width, Style.EMPTY).stream())
			.collect(Collectors.toList());
	}

	public static List<ITextProperties> expandNewlines(ITextComponent... descriptionComponents) {
		List<ITextProperties> descriptionLinesExpanded = new ArrayList<>();
		for (ITextComponent descriptionLine : descriptionComponents) {
			ExpandNewLineTextAcceptor newLineTextAcceptor = new ExpandNewLineTextAcceptor();
			descriptionLine.visit(newLineTextAcceptor, Style.EMPTY);
			newLineTextAcceptor.addLinesTo(descriptionLinesExpanded);
		}
		return descriptionLinesExpanded;
	}
}
