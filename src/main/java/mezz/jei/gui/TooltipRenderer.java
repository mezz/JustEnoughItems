package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.config.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		drawHoveringText(poseStack, textLines, x, y, Constants.MAX_TOOLTIP_WIDTH, minecraft.font, ItemStack.EMPTY);
	}

	public static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, Font font) {
		drawHoveringText(poseStack, textLines, x, y, Constants.MAX_TOOLTIP_WIDTH, font, ItemStack.EMPTY);
	}

	public static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, Font font, Object ingredient) {
		drawHoveringText(poseStack, textLines, x, y, Constants.MAX_TOOLTIP_WIDTH, font, ingredient);
	}

	public static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, int maxWidth, Font font, Object ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		// text wrapping
		if (maxWidth > 0) {
			boolean needsWrap = textLines.stream()
				.anyMatch(line -> font.width(line) > maxWidth);

			if (needsWrap) {
				StringSplitter splitter = font.getSplitter();
				textLines = textLines.stream()
					.flatMap(text -> splitter.splitLines(text, maxWidth, Style.EMPTY).stream())
					.toList();
			}
		}

		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		screen.renderComponentTooltip(poseStack, textLines, x, y, font, itemStack);
	}
}
