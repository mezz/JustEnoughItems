package mezz.jei.gui;

import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Workaround for GuiScreen.drawHoveringText being protected instead of public.
 * (the method with FontRenderer is added by Forge and can't be AT'd).
 */
public class TooltipRenderer {
	private static final TooltipGuiScreen tooltipScreen = new TooltipGuiScreen();

	public static void drawHoveringText(@Nonnull Minecraft minecraft, @Nonnull String textLine, int x, int y) {
		List<String> textLines = minecraft.fontRendererObj.listFormattedStringToWidth(textLine, Constants.MAX_TOOLTIP_WIDTH);
		drawHoveringText(minecraft, textLines, x, y, minecraft.fontRendererObj);
	}

	public static void drawHoveringText(@Nonnull Minecraft minecraft, @Nonnull List<String> textLines, int x, int y) {
		drawHoveringText(minecraft, textLines, x, y, minecraft.fontRendererObj);
	}

	public static void drawHoveringText(@Nonnull Minecraft minecraft, @Nonnull List<String> textLines, int x, int y, @Nonnull FontRenderer font) {
		tooltipScreen.set(minecraft);
		tooltipScreen.drawHoveringText(textLines, x, y, font);
	}

	private static class TooltipGuiScreen extends GuiScreen {
		public void set(@Nonnull Minecraft minecraft) {
			this.mc = minecraft;
			this.itemRender = minecraft.getRenderItem();
			this.width = minecraft.currentScreen.width;
			this.height = minecraft.currentScreen.height;
		}

		@Override
		public void drawHoveringText(@Nonnull List<String> textLines, int x, int y, @Nonnull FontRenderer font) {
			super.drawHoveringText(textLines, x, y, font);
		}
	}
}
