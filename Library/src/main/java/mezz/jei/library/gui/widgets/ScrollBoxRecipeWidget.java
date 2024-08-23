package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix4f;

public class ScrollBoxRecipeWidget extends AbstractScrollWidget implements IScrollBoxWidget, IJeiInputHandler {
	private final int visibleHeight;
	private final int hiddenHeight;
	private final ScreenRectangle contentsArea;
	private final IDrawable contents;

	public ScrollBoxRecipeWidget(IDrawable contents, int visibleHeight, int xPos, int yPos) {
		super(new ScreenRectangle(
			xPos,
			yPos,
			contents.getWidth() + AbstractScrollWidget.getScrollBoxScrollbarExtraWidth(),
			visibleHeight
		));
		this.contents = contents;
		this.visibleHeight = visibleHeight;
		this.contentsArea = new ScreenRectangle(
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
	protected void drawContents(GuiGraphics guiGraphics, double mouseX, double mouseY, float scrollOffsetY) {
		PoseStack poseStack = guiGraphics.pose();
		PoseStack.Pose last = poseStack.last();
		Matrix4f pose = last.pose();

		ScreenRectangle scissorArea = MathUtil.transform(contentsArea, pose);
		guiGraphics.enableScissor(
			scissorArea.left(),
			scissorArea.top(),
			scissorArea.right(),
			scissorArea.bottom()
		);
		poseStack.pushPose();
		float scrollAmount = hiddenHeight * scrollOffsetY;
		poseStack.translate(0.0, -scrollAmount, 0.0);
		try {
			contents.draw(guiGraphics);
		} finally {
			poseStack.popPose();
			guiGraphics.disableScissor();
		}
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
