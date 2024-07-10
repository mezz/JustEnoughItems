package mezz.jei.common.gui;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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

	public static <T> void drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		drawHoveringText(guiGraphics, textLines, x, y, typedIngredient, ingredientRenderer);
	}

	public static <T> void drawHoveringText(GuiGraphics guiGraphics, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		T ingredient = typedIngredient.getIngredient();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack i ? i : ItemStack.EMPTY;
		try {
			drawHoveringText(guiGraphics, textLines, x, y, itemStack, font);
		} catch (RuntimeException e) {
			IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
			CrashReport crashReport = ErrorUtil.createIngredientCrashReport(e, "Rendering ingredient tooltip", ingredientManager, typedIngredient);
			throw new ReportedException(crashReport);
		}
	}

	public static <T> void drawHoveringTooltip(GuiGraphics guiGraphics, List<ClientTooltipComponent> components, int x, int y, ITypedIngredient<T> typedIngredient, IIngredientRenderer<T> ingredientRenderer, IIngredientManager ingredientManager){
		Minecraft minecraft = Minecraft.getInstance();
		T ingredient = typedIngredient.getIngredient();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack i ? i : ItemStack.EMPTY;
		try {
			drawHoveringTooltip(guiGraphics, components, x, y, itemStack, font);
		} catch (RuntimeException e) {
			CrashReport crashReport = ErrorUtil.createIngredientCrashReport(e, "Rendering ingredient tooltip", ingredientManager, typedIngredient);
			throw new ReportedException(crashReport);
		}
	}

	private static void drawHoveringTooltip(GuiGraphics guiGraphics, List<ClientTooltipComponent> components, int x, int y, ItemStack itemStack, Font font){
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.renderTooltip(screen, guiGraphics, components, x, y, font, itemStack);
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
