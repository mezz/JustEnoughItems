package mezz.jei.common.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

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

	public static Component truncateStringToWidth(Component text, int width, Font fontRenderer) {
		int ellipsisWidth = fontRenderer.width("...");
		FormattedText truncatedText = fontRenderer.substrByWidth(text, width - ellipsisWidth);
		String truncatedTextString = truncatedText.getString();
		return Component.literal(truncatedTextString + "...");
	}

	public static List<FormattedText> splitLines(List<FormattedText> lines, int width) {
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

	public static String intsToString(Collection<Integer> indexes) {
		return indexes.stream()
			.sorted()
			.map(i -> Integer.toString(i))
			.collect(Collectors.joining(", "));
	}


	public static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, String string, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, string);
		Screen.drawString(poseStack, font, string, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}

	public static void drawCenteredStringWithShadow(PoseStack poseStack, Font font, Component text, ImmutableRect2i area) {
		ImmutableRect2i textArea = MathUtil.centerTextArea(area, font, text);
		Screen.drawString(poseStack, font, text, textArea.getX(), textArea.getY(), 0xFFFFFFFF);
	}
}
