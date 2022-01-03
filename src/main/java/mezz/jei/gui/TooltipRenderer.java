package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Constants;
import mezz.jei.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		drawHoveringText(poseStack, textLines, x, y, ItemStack.EMPTY, font);
	}

	public static <T> void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, T ingredient, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringText(poseStack, textLines, x, y, itemStack, font);
	}

	private static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, ItemStack itemStack, Font font) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		textLines = StringUtil.splitLines(textLines, Constants.MAX_TOOLTIP_WIDTH);
		screen.renderComponentTooltip(poseStack, textLines, x, y, font, itemStack);
	}
}
