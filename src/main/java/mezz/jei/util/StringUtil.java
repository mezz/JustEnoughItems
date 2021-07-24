package mezz.jei.util;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;

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
}
