package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.fmlclient.gui.GuiUtils;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(FormattedText textLine, int x, int y, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, minecraft.font, poseStack);
	}

	public static void drawHoveringText(List<? extends FormattedText> textLines, int x, int y, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, -1, minecraft.font, poseStack);
	}

	public static void drawHoveringText(List<? extends FormattedText> textLines, int x, int y, int maxWidth, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, maxWidth, minecraft.font, poseStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends FormattedText> textLines, int x, int y, Font font, PoseStack poseStack) {
		drawHoveringText(ingredient, textLines, x, y, -1, font, poseStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends FormattedText> textLines, int x, int y, int maxWidth, Font font, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		GuiUtils.drawHoveringText(itemStack, poseStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
	}
}
