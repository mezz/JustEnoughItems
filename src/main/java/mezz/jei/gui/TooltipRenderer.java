package mezz.jei.gui;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;

import mezz.jei.util.ErrorUtil;
import mezz.jei.util.LimitedLogger;
import mezz.jei.util.Log;
import org.apache.logging.log4j.Level;

public final class TooltipRenderer {
	private static final LimitedLogger LIMITED_LOGGER = new LimitedLogger(Log.get(), Duration.ofSeconds(30));

	private TooltipRenderer() {
	}

	public static void drawHoveringText(Minecraft minecraft, String textLine, int x, int y) {
		drawHoveringText(ItemStack.EMPTY, minecraft, Collections.singletonList(textLine), x, y, -1, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, -1, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, maxWidth, minecraft.fontRenderer);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, -1, font);
	}

	public static void drawHoveringText(Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
		drawHoveringText(ItemStack.EMPTY, minecraft, textLines, x, y, maxWidth, font);
	}

	public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(itemStack, itemStack, minecraft, textLines, x, y, -1, font);
	}

	public static void drawHoveringText(ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
		drawHoveringText(itemStack, itemStack, minecraft, textLines, x, y, maxWidth, font);
	}

	public static <T> void drawHoveringText(T ingredient, Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringText(ingredient, itemStack, minecraft, textLines, x, y, -1, font);
	}

	public static <T> void drawHoveringText(T ingredient, ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, FontRenderer font) {
		drawHoveringText(ingredient, itemStack, minecraft, textLines, x, y, -1, font);
	}

	private static void drawHoveringText(@Nullable Object ingredient, ItemStack itemStack, Minecraft minecraft, List<String> textLines, int x, int y, int maxWidth, FontRenderer font) {
		List<String> safeTextLines = new ArrayList<>(textLines.size());
		for (String textLine : textLines) {
			if (textLine != null) {
				safeTextLines.add(textLine);
			}
		}
		if (safeTextLines.isEmpty()) {
			return;
		}
		ScaledResolution scaledresolution = new ScaledResolution(minecraft);
		try {
			GuiUtils.drawHoveringText(itemStack, safeTextLines, x, y, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), maxWidth, font);
		} catch (RuntimeException | LinkageError e) {
			String stringTooltip = safeTextLines.stream().collect(Collectors.joining("\n"));
			String ingredientInfo = getIngredientInfo(ingredient);
			String message = String.format("Failed to render tooltip for %s:\n%s", ingredientInfo, stringTooltip);
			LIMITED_LOGGER.log(Level.ERROR, message, message, e);
		}
	}

	private static String getIngredientInfo(@Nullable Object ingredient) {
		if (ingredient == null || ingredient == ItemStack.EMPTY) {
			return "JEI";
		}
		try {
			return ErrorUtil.getIngredientInfo(ingredient);
		} catch (RuntimeException | LinkageError e) {
			return ingredient.getClass().getName();
		}
	}
}
