package mezz.jei.gui;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

public class TooltipRenderer {
	public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
		drawHoveringText(minecraft, Collections.singletonList(textLine), x, y, minecraft.fontRendererObj);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y) {
		drawHoveringText(minecraft, textLines, x, y, minecraft.fontRendererObj);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(null, minecraft, textLines, x, y, font);
	}

	public static void drawHoveringText(@Nullable ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		ScaledResolution scaledresolution = new ScaledResolution(minecraft);
		GuiUtils.drawHoveringText(itemStack, textLines, x, y, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), -1, font);
	}
}
