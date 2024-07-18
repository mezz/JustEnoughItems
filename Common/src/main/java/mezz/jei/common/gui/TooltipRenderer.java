package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.util.LimitedLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TooltipRenderer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger LIMITED_LOGGER = new LimitedLogger(LOGGER, Duration.ofSeconds(30));

	private TooltipRenderer() {
	}

	public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}
		Font font = minecraft.font;
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		try {
			renderHelper.renderTooltip(screen, poseStack, textLines, Optional.empty(), x, y, font, ItemStack.EMPTY);
		} catch (RuntimeException e) {
			String stringTooltip = getTooltipDebugString(textLines, "\n");
			String message = "Failed to render tooltip:\n" + stringTooltip;
			LIMITED_LOGGER.log(Level.ERROR, message, message, stringTooltip, e);
		}
	}

	public static <T> void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		drawHoveringText(poseStack, textLines, x, y, typedIngredient, ingredientRenderer);
	}

	public static <T> void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		T ingredient = typedIngredient.getIngredient();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		drawHoveringText(poseStack, textLines, x, y, typedIngredient, font);
	}

	private static <T> void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient, Font font) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		ItemStack itemStack = typedIngredient.getItemStack().orElse(ItemStack.EMPTY);
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		try {
			Optional<TooltipComponent> tooltipImage = itemStack.getTooltipImage();
			renderHelper.renderTooltip(screen, poseStack, textLines, tooltipImage, x, y, font, itemStack);
		} catch (RuntimeException e) {
			T ingredient = typedIngredient.getIngredient();
			IIngredientType<T> type = typedIngredient.getType();
			IIngredientManager ingredientManager = Internal.getIngredientManager();
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, type, ingredientManager);
			String stringTooltip = getTooltipDebugString(textLines, "\n");
			String message = String.format("Failed to render tooltip for ingredient %s:\n%s", ingredientInfo, stringTooltip);
			LIMITED_LOGGER.log(Level.ERROR, message, message, e);
		}
	}

	private static String getTooltipDebugString(List<Component> textLines, String joinDelimiter) {
		return textLines.stream()
			.map(Component::plainCopy)
			.map(MutableComponent::toString)
			.collect(Collectors.joining(joinDelimiter));
	}
}
