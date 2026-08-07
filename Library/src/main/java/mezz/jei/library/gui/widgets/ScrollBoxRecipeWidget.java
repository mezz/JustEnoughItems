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
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.DrawableWrappedText;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public class ScrollBoxRecipeWidget extends AbstractScrollWidget implements IScrollBoxWidget, IJeiInputHandler {
	private IDrawable contents = DrawableBlank.EMPTY;

	public ScrollBoxRecipeWidget(int width, int height, int xPos, int yPos) {
		super(new ImmutableRect2i(xPos, yPos, width, height));
	}

	@Override
	public int getContentAreaWidth() {
		return contentsArea.width();
	}

	@Override
	public int getContentAreaHeight() {
		return contentsArea.height();
	}

	@Override
	public IScrollBoxWidget setContents(IDrawable contents) {
		this.contents = contents;
		return this;
	}

	@Override
	public IScrollBoxWidget setContents(List<FormattedText> text) {
		this.contents = new DrawableWrappedText(text, getContentAreaWidth());
		return this;
	}

	@Override
	protected int getVisibleAmount() {
		return contentsArea.height();
	}

	@Override
	protected int getHiddenAmount() {
		return Math.max(contents.getHeight() - contentsArea.height(), 0);
	}

	@Override
	protected void drawContents(PoseStack poseStack, double mouseX, double mouseY, float scrollOffsetY) {
		PoseStack.Pose last = poseStack.last();
		Matrix4f pose = last.pose();

		ImmutableRect2i scissorArea = MathUtil.transform(contentsArea, pose);
		enableScissor(scissorArea);
		poseStack.pushPose();
		float scrollAmount = getHiddenAmount() * scrollOffsetY;
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
