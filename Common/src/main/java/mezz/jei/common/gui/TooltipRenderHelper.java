package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import mezz.jei.common.util.ImmutablePoint2i;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public final class TooltipRenderHelper {
	private static final ThreadLocal<TooltipPositioner> POSITIONER = new ThreadLocal<>();

	public static @Nullable TooltipPositioner getPositioner() {
		return POSITIONER.get();
	}

	public static void withPositioner(@Nullable TooltipPositioner positioner, Runnable draw) {
		TooltipPositioner previous = POSITIONER.get();
		if (positioner == null) {
			POSITIONER.remove();
		} else {
			POSITIONER.set(positioner);
		}
		try {
			draw.run();
		} finally {
			if (previous == null) {
				POSITIONER.remove();
			} else {
				POSITIONER.set(previous);
			}
		}
	}

	public static ImmutablePoint2i positionTooltip(int screenWidth, int screenHeight, int x, int y, int width, int height) {
		TooltipPositioner positioner = POSITIONER.get();
		if (positioner != null) {
			return positioner.positionTooltip(screenWidth, screenHeight, x, y, width, height);
		}
		return defaultPosition(screenWidth, screenHeight, x, y, width, height);
	}

	public static ImmutablePoint2i defaultPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
		int left = x + 12;
		int top = y - 12;
		if (left + width > screenWidth) {
			left -= 28 + width;
		}
		if (top + height + 6 > screenHeight) {
			top = screenHeight - height - 6;
		}
		return new ImmutablePoint2i(left, top);
	}

	public static void renderTooltip(Screen screen, PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, Font font, BiConsumer<Integer, Integer> draw) {
		if (POSITIONER.get() == null || components.isEmpty()) {
			draw.accept(x, y);
			return;
		}
		int width = components.stream().mapToInt(line -> line.getWidth(font)).max().orElse(0);
		int height = components.stream().mapToInt(ClientTooltipComponent::getHeight).sum();
		if (components.size() == 1) {
			height -= 2;
		}
		ImmutablePoint2i positioned = positionTooltip(screen.width, screen.height, x, y, width, height);
		// Supply an anchor whose native tooltip origin is the selected position. Components then
		// receive their actual screen coordinates for both drawing and hit testing.
		int top = Math.min(positioned.y(), screen.height - height - 6);
		draw.accept(positioned.x() - 12, top + 12);
	}

	/**
	 * Minecraft 1.19 passes the tooltip depth to image components without applying it to their pose stack.
	 * Backported JEI components render through the pose stack, so apply that depth here and remove the matching
	 * legacy item-renderer offset to keep ingredients on the same relative layers as the 1.20 GuiGraphics renderer.
	 */
	public static void renderImage(PoseStack poseStack, ItemRenderer itemRenderer, int z, Runnable draw) {
		float originalBlitOffset = itemRenderer.blitOffset;
		poseStack.pushPose();
		try {
			poseStack.translate(0, 0, z);
			itemRenderer.blitOffset = originalBlitOffset - z;
			draw.run();
		} finally {
			itemRenderer.blitOffset = originalBlitOffset;
			poseStack.popPose();
		}
	}

	private TooltipRenderHelper() {
	}
}
