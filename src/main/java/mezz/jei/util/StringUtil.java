package mezz.jei.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public final class StringUtil {
	private StringUtil() {

	}

	public static ITextComponent truncateStringToWidth(ITextComponent text, int width, FontRenderer fontRenderer) {
		return new StringTextComponent(fontRenderer.func_238417_a_(text, width - fontRenderer.getStringWidth("...")).getString() + "...");
	}
}
