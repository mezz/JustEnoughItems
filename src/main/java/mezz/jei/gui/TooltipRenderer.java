package mezz.jei.gui;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(String textLine, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, minecraft.fontRenderer);
	}

	public static void drawHoveringText(List<String> textLines, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, -1, minecraft.fontRenderer);
	}

	public static void drawHoveringText(List<String> textLines, int x, int y, int maxWidth) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, maxWidth, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Object ingredient, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(ingredient, textLines, x, y, -1, font);
	}

	public static void drawHoveringText(Object ingredient, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getMainWindow().getScaledWidth();
		int scaledHeight = minecraft.getMainWindow().getScaledHeight();
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		GuiUtils.drawHoveringText(itemStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
	}
}
