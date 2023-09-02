package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

	public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		drawHoveringText(poseStack, textLines, x, y, ItemStack.EMPTY, font);
	}

	public static <T> void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ITypedIngredient<T> typedIngredient, IIngredientManager ingredientManager) {
		IIngredientType<T> ingredientType = typedIngredient.getType();
		T ingredient = typedIngredient.getIngredient();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		drawHoveringText(poseStack, textLines, x, y, ingredient, ingredientRenderer);
	}

	public static <T> void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, T ingredient, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringText(poseStack, textLines, x, y, itemStack, font);
	}

	public static <T> void drawHoveringComponent(PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, T ingredient, IIngredientRenderer<T> ingredientRenderer) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = ingredient instanceof ItemStack ? (ItemStack) ingredient : ItemStack.EMPTY;
		drawHoveringComponent(poseStack, components, x, y, itemStack, font);
	}

	private static void drawHoveringComponent(PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, ItemStack itemStack, Font font) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.renderTooltipComponent(screen, poseStack, components, x, y, font, itemStack);
	}

	private static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ItemStack itemStack, Optional<TooltipComponent> tooltipImage, Font font) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		if (screen == null) {
			return;
		}

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		renderHelper.renderTooltip(screen, poseStack, textLines, tooltipImage, x, y, font, itemStack);
	}

	private static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y, ItemStack itemStack, Font font) {
		drawHoveringText(poseStack, textLines, x, y, itemStack, itemStack.getTooltipImage(), font);
	}

}
