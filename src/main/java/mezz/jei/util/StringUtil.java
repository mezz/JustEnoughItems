package mezz.jei.util;

import net.minecraft.client.gui.FontRenderer;

public class StringUtil {

	public static void drawCenteredString(FontRenderer fontRenderer, String string, int guiWidth, int yPos, int color, boolean shadow) {
		fontRenderer.drawString(string, (guiWidth - fontRenderer.getStringWidth(string)) / 2, yPos, color, shadow);
	}

}
