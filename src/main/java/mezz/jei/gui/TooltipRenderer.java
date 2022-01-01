package mezz.jei.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.config.Constants;
import mezz.jei.ingredients.IngredientManager;
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
		drawHoveringText(poseStack, textLines, x, y, Constants.MAX_TOOLTIP_WIDTH, ItemStack.EMPTY);
	}

	public static void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, Object ingredient) {
		drawHoveringText(poseStack, textLines, x, y, Constants.MAX_TOOLTIP_WIDTH, ingredient);
	}

	public static <T> void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, int maxWidth, T ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}
		Font font = getFont(ingredient);
		drawHoveringText(poseStack, textLines, x, y, maxWidth, ingredient, font);
	}

	private static <T> void drawHoveringText(PoseStack poseStack, List<? extends FormattedText> textLines, int x, int y, int maxWidth, T ingredient, Font font) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		textLines = StringUtil.splitLines(textLines, maxWidth);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		screen.renderComponentTooltip(poseStack, textLines, x, y, font, itemStack);
	}

	private static <T> Font getFont(T ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient);
		return ingredientRenderer.getFontRenderer(minecraft, ingredient);
	}
}
