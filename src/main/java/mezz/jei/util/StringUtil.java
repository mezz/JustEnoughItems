package mezz.jei.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

public final class StringUtil {
	private StringUtil() {

	}

	public static ITextComponent stripStyling(ITextComponent textComponent) {
		IFormattableTextComponent text = textComponent.copyRaw();
		for (ITextComponent sibling : textComponent.getSiblings()) {
			text.appendSibling(stripStyling(sibling));
		}
		return text;
	}

	public static ITextComponent truncateStringToWidth(ITextComponent text, int width, FontRenderer fontRenderer) {
		int ellipsisWidth = fontRenderer.getStringWidth("...");
		ITextProperties truncatedText = fontRenderer.func_238417_a_(text, width - ellipsisWidth);
		String truncatedTextString = truncatedText.getString();
		return new StringTextComponent(truncatedTextString + "...");
	}
}
