package mezz.jei.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

/**
 * Workaround for GuiScreen.drawHoveringText being protected instead of public.
 * (the method with FontRenderer is added by Forge and can't be AT'd).
 */
public class TooltipRenderer {
	private static final TooltipGuiScreen tooltipScreen = new TooltipGuiScreen();

	public static void drawHoveringText(Minecraft minecraft, List textLines, int x, int y, FontRenderer font) {
		tooltipScreen.set(minecraft);
		tooltipScreen.drawHoveringText(textLines, x, y, font);
	}

	private static class TooltipGuiScreen extends GuiScreen {
		public void set(Minecraft minecraft) {
			this.mc = minecraft;
			this.itemRender = minecraft.getRenderItem();
			this.width = minecraft.currentScreen.width;
			this.height = minecraft.currentScreen.height;
		}

		@Override
		public void drawHoveringText(List textLines, int x, int y, FontRenderer font) {
			super.drawHoveringText(textLines, x, y, font);
		}
	}
}
