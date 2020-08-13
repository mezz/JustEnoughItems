package mezz.jei.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(ITextProperties textLine, int x, int y, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, minecraft.fontRenderer, matrixStack);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, -1, minecraft.fontRenderer, matrixStack);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, maxWidth, minecraft.fontRenderer, matrixStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends ITextProperties> textLines, int x, int y, FontRenderer font, MatrixStack matrixStack) {
		drawHoveringText(ingredient, textLines, x, y, -1, font, matrixStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends ITextProperties> textLines, int x, int y, int maxWidth, FontRenderer font, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getMainWindow().getScaledWidth();
		int scaledHeight = minecraft.getMainWindow().getScaledHeight();
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		
		// Vanilla workaround for tool tips. Use the below when forge adds it back again
		if(minecraft.currentScreen != null)
			minecraft.currentScreen.renderToolTip(matrixStack, textLines.stream().map(LanguageMap.getInstance()::func_241870_a).collect(ImmutableList.toImmutableList()), x, y, font);
		
		// TODO use again when available 
		//GuiUtils.drawHoveringText(itemStack, matrixStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
	}
}
