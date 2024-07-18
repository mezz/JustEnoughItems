package mezz.jei.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LimitedLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TooltipRenderer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger LIMITED_LOGGER = new LimitedLogger(LOGGER, Duration.ofSeconds(30));

	private TooltipRenderer() {
	}

	public static void drawHoveringText(ITextProperties textLine, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(null, ItemStack.EMPTY, Collections.singletonList(textLine), x, y, -1, matrixStack, Minecraft.getInstance().font);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack) {
		drawHoveringText(null, ItemStack.EMPTY, textLines, x, y, -1, matrixStack, Minecraft.getInstance().font);
	}

	public static void drawHoveringText(List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack) {
		drawHoveringText(null, ItemStack.EMPTY, textLines, x, y, maxWidth, matrixStack, Minecraft.getInstance().font);
	}

	public static <T> void drawHoveringText(T ingredient, List<? extends ITextProperties> textLines, int x, int y, MatrixStack matrixStack, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		FontRenderer font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringText(ingredient, itemStack, textLines, x, y, -1, matrixStack, font);
	}

	private static void drawHoveringText(@Nullable Object ingredient, ItemStack itemStack, List<? extends ITextProperties> textLines, int x, int y, int maxWidth, MatrixStack matrixStack, FontRenderer font) {
		Minecraft minecraft = Minecraft.getInstance();
		int scaledWidth = minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
		try {
			GuiUtils.drawHoveringText(itemStack, matrixStack, textLines, x, y, scaledWidth, scaledHeight, maxWidth, font);
		} catch (RuntimeException | LinkageError e) {
			String stringTooltip = textLines.stream()
				.map(Object::toString)
				.collect(Collectors.joining("\n"));
			String ingredientInfo = getIngredientInfo(ingredient);
			String message = String.format("Failed to render tooltip for %s:\n%s", ingredientInfo, stringTooltip);
			LIMITED_LOGGER.log(Level.ERROR, message, message, e);
		}
	}

	private static String getIngredientInfo(@Nullable Object ingredient) {
		if (ingredient == null) {
			return "JEI";
		}
		try {
			return ErrorUtil.getIngredientInfo(ingredient);
		} catch (RuntimeException | LinkageError e) {
			return ingredient.getClass().getName();
		}
	}
}
