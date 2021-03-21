package mezz.jei.util;

import net.minecraft.client.gui.FontRenderer;

public final class StringUtil {
	private StringUtil() {

	}

	public static String truncateStringToWidth(String string, int width, FontRenderer fontRenderer) {
		return fontRenderer.getLineScrollOffset(string, width - fontRenderer.getStringWidth("..."), false) + "...";
	}
}
