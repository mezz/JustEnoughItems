package mezz.jei.common.gui;

import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class TooltipRenderer {
	private TooltipRenderer() {
	}

	public static void drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		drawHoveringText(guiGraphics, textLines, x, y, ItemStack.EMPTY, font);
	}

	public static <T> void drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		drawHoveringText(guiGraphics, textLines, x, y, ingredient, ingredientRenderer);
	}

	public static <T> void drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, int x, int y, T ingredient, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringText(guiGraphics, textLines, x, y, itemStack, font);
	}

	private static void drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, int x, int y, ItemStack itemStack, Font font) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		Optional<TooltipComponent> tooltipImage = itemStack.getTooltipImage();
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.renderTooltip(screen, guiGraphics, textLines, tooltipImage, x, y, font, itemStack);
	}
}
