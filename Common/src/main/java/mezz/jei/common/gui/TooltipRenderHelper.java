package mezz.jei.common.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;

public final class TooltipRenderHelper {
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
