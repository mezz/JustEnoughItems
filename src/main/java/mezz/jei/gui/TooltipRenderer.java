package mezz.jei.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class TooltipRenderer {
	public static void drawHoveringText(@Nonnull Minecraft minecraft, @Nonnull String textLine, int x, int y) {
		drawHoveringText(minecraft, Collections.singletonList(textLine), x, y, minecraft.fontRendererObj);
	}

	public static void drawHoveringText(@Nonnull Minecraft minecraft, @Nonnull List<String> textLines, int x, int y) {
		drawHoveringText(minecraft, textLines, x, y, minecraft.fontRendererObj);
	}

	public static void drawHoveringText(@Nonnull Minecraft minecraft, @Nonnull List<String> textLines, int x, int y, @Nonnull FontRenderer font) {
		ScaledResolution scaledresolution = new ScaledResolution(minecraft);
		GuiUtils.drawHoveringText(textLines, x, y, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), -1, font);
	}
}
