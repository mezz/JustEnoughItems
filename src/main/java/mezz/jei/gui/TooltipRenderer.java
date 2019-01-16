package mezz.jei.gui;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
		drawHoveringText(ItemStack.EMPTY, minecraft, Collections.singletonList(textLine), x, y, -1, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, -1, font);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, maxWidth, font);
	}

	public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(itemStack, minecraft, textLines, x, y, -1, font);
	}

	public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
		ScaledResolution scaledresolution = new ScaledResolution(minecraft);
		GuiUtils.drawHoveringText(itemStack, textLines, x, y, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), maxWidth, font);
	}
}
