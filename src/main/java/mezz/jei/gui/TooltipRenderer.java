package mezz.jei.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(ITextProperties textLine, int x, int y, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, minecraft.font, matrixStack);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, -1, minecraft.font, matrixStack);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, maxWidth, minecraft.font, matrixStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends ITextProperties> textLines, int x, int y, FontRenderer font, MatrixStack matrixStack) {
		drawHoveringText(ingredient, textLines, x, y, -1, font, matrixStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends ITextProperties> textLines, int x, int y, int maxWidth, FontRenderer font, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		GuiUtils.drawHoveringText(itemStack, matrixStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
	}
}
