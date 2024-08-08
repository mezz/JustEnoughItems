package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

public class ScrollBoxRecipeWidget extends AbstractScrollWidget implements IScrollBoxWidget, IJeiInputHandler {
	private final int visibleHeight;
	private final int hiddenHeight;
	private final ImmutableRect2i contentsArea;
	private final IDrawable contents;

	public ScrollBoxRecipeWidget(IDrawable contents, int visibleHeight, int xPos, int yPos) {
		super(new Rect2i(
			xPos,
			yPos,
			contents.getWidth() + AbstractScrollWidget.getScrollBoxScrollbarExtraWidth(),
			visibleHeight
		));
		this.contents = contents;
		this.visibleHeight = visibleHeight;
		this.contentsArea = new ImmutableRect2i(
			0,
			0,
			contents.getWidth(),
			visibleHeight
		);
		this.hiddenHeight = Math.max(contents.getHeight() - visibleHeight, 0);
	}

	@Override
	protected int getVisibleAmount() {
		return visibleHeight;
	}

	@Override
	protected int getHiddenAmount() {
		return hiddenHeight;
	}

	@Override
	protected void drawContents(PoseStack poseStack, double mouseX, double mouseY, float scrollOffsetY) {
		PoseStack.Pose last = poseStack.last();
		Matrix4f pose = last.pose();

		ImmutableRect2i scissorArea = MathUtil.transform(contentsArea, pose);
		enableScissor(scissorArea);
		poseStack.pushPose();
		float scrollAmount = hiddenHeight * scrollOffsetY;
		poseStack.translate(0.0, -scrollAmount, 0.0);
		try {
			contents.draw(poseStack);
		} finally {
			poseStack.popPose();
			RenderSystem.disableScissor();
		}
	}

	private static void enableScissor(ImmutableRect2i area) {
		Window window = Minecraft.getInstance().getWindow();
		double scale = window.getGuiScale();
		int x = (int) (area.getX() * scale);
		int y = (int) (window.getHeight() - ((area.getY() + area.getHeight()) * scale));
		int width = (int) (area.getWidth() * scale);
		int height = (int) (area.getHeight() * scale);
		RenderSystem.enableScissor(x, y, width, height);
	}

	@Override
	protected float calculateScrollAmount(double scrollDeltaY) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		int smoothScrollRate = clientConfig.getSmoothScrollRate();

		int totalHeight = contents.getHeight();
		double scrollAmount = scrollDeltaY * smoothScrollRate;
		return (float) (scrollAmount / (double) totalHeight);
	}
}
