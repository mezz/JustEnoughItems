package mezz.jei.util;

import net.minecraft.client.gui.FontRenderer;

public final class StringUtil {
	private StringUtil() {

	}

	public static String truncateStringToWidth(String string, int width, FontRenderer fontRenderer) {
		return fontRenderer.func_238413_a_(string, width - fontRenderer.getStringWidth("..."), false) + "...";
	}
}
