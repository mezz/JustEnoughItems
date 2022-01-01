package mezz.jei.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.ingredients.IngredientManager;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(ITextProperties textLine, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, matrixStack);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, -1, matrixStack);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack) {
		drawHoveringText(ItemStack.EMPTY, textLines, x, y, maxWidth, matrixStack);
	}

	public static void drawHoveringText(Object ingredient, List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(ingredient, textLines, x, y, -1, matrixStack);
	}

	private static void drawHoveringText(Object ingredient, List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		FontRenderer font = getFont(ingredient);
		GuiUtils.drawHoveringText(itemStack, matrixStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
	}

	private static <T> FontRenderer getFont(T ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		IngredientManager ingredientManager = Internal.getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient);
		return ingredientRenderer.getFontRenderer(minecraft, ingredient);
	}
}
