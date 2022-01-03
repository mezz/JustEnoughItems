package mezz.jei.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.Collections;
import java.util.List;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(ITextProperties textLine, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, matrixStack, Minecraft.getInstance().font);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, -1, matrixStack, Minecraft.getInstance().font);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack) {
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, maxWidth, matrixStack, Minecraft.getInstance().font);
	}

	public static <T> void drawHoveringText(T ingredient, List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringText(itemStack, textLines, x, y, -1, matrixStack, font);
	}

	private static void drawHoveringText(ItemStack itemStack, List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack, FontRenderer font) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
		GuiUtils.drawHoveringText(itemStack, matrixStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
	}
}
