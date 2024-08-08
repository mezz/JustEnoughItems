package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IScrollBoxWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ScrollBoxRecipeWidget implements IScrollBoxWidget, IJeiInputHandler {
	private static final int SCROLLBAR_PADDING = 2;
	private static final int SCROLLBAR_WIDTH = 14;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;

	public static int getScrollBoxScrollbarExtraWidth() {
		return SCROLLBAR_WIDTH + SCROLLBAR_PADDING;
	}

	private final DrawableNineSliceTexture scrollbarMarker;
	private final DrawableNineSliceTexture scrollbarBackground;
	private final int visibleHeight;
	private final int hiddenHeight;
	private final ScreenRectangle area;
	private final ScreenRectangle contentsArea;
	private final ImmutableRect2i scrollArea;
	private final IDrawable contents;
	/**
	 * Position of the mouse on the scroll marker when dragging.
	 */
	private double dragOriginY = -1;
	/**
	 * Amount scrolled in percent, (0 = top, 1 = bottom)
	 */
	private float scrollOffsetY = 0;

	public ScrollBoxRecipeWidget(IDrawable contents, int visibleHeight, int xPos, int yPos) {
		Textures textures = Internal.getTextures();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.contents = contents;
		this.visibleHeight = visibleHeight;
		this.area = new ScreenRectangle(
			xPos,
			yPos,
			contents.getWidth() + getScrollBoxScrollbarExtraWidth(),
			visibleHeight
		);
		this.contentsArea = new ScreenRectangle(
			0,
			0,
			contents.getWidth(),
			visibleHeight
		);
		this.scrollArea = calculateScrollArea(area.width(), area.height());
		this.hiddenHeight = Math.max(contents.getHeight() - visibleHeight, 0);
	}

	@Override
	public ScreenPosition getPosition() {
		return area.position();
	}

	@Override
	public ScreenRectangle getArea() {
		return area;
	}

	private static ImmutableRect2i calculateScrollArea(int width, int height) {
		return new ImmutableRect2i(
			width - SCROLLBAR_WIDTH,
			0,
			SCROLLBAR_WIDTH,
			height
		);
	}

	private ImmutableRect2i calculateScrollbarMarkerArea() {
		int totalSpace = scrollArea.height() - 2;
		int scrollMarkerWidth = scrollArea.width() - 2;
		int scrollMarkerHeight = Math.round(totalSpace * (visibleHeight / (float) (visibleHeight + hiddenHeight)));
		scrollMarkerHeight = Math.max(scrollMarkerHeight, MIN_SCROLL_MARKER_HEIGHT);
		int scrollbarMarkerY = Math.round((totalSpace - scrollMarkerHeight) * scrollOffsetY);
		return new ImmutableRect2i(
			scrollArea.getX() + 1,
			scrollArea.getY() + 1 + scrollbarMarkerY,
			scrollMarkerWidth,
			scrollMarkerHeight
		);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		scrollbarBackground.draw(guiGraphics, scrollArea);

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();
		scrollbarMarker.draw(guiGraphics, scrollbarMarkerArea);

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

	private static float subtractInputFromScroll(int totalHeight, float scrollOffset, double scrollDeltaY) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		int smoothScrollRate = clientConfig.getSmoothScrollRate();

		double scrollAmount = scrollDeltaY * smoothScrollRate;
		float newScrollOffset = scrollOffset - (float) (scrollAmount / (double) totalHeight);
		return Mth.clamp(newScrollOffset, 0.0F, 1.0F);
	}

	@Override
	public boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
		if (!userInput.is(Internal.getKeyMappings().getLeftClick())) {
			return false;
		}
		if (!userInput.isSimulate()) {
			dragOriginY = -1;
		}

		if (scrollArea.contains(mouseX, mouseY)) {
			if (hiddenHeight == 0) {
				return false;
			}

			if (userInput.isSimulate()) {
				ImmutableRect2i scrollMarkerArea = calculateScrollbarMarkerArea();
				if (!scrollMarkerArea.contains(mouseX, mouseY)) {
					moveScrollbarCenterTo(scrollMarkerArea, mouseY);
					scrollMarkerArea = calculateScrollbarMarkerArea();
				}
				dragOriginY = mouseY - scrollMarkerArea.y();
			}
			return true;
		}
		return false;
	}

	private void moveScrollbarCenterTo(ImmutableRect2i scrollMarkerArea, double centerY) {
		double topY = centerY - (scrollMarkerArea.height() / 2.0);
		moveScrollbarTo(scrollMarkerArea, topY);
	}

	private void moveScrollbarTo(ImmutableRect2i scrollMarkerArea, double topY) {
		int minY = scrollArea.y();
		int maxY = scrollArea.y() + scrollArea.height() - scrollMarkerArea.height();
		double relativeY = topY - minY;
		int totalSpace = maxY - minY;
		scrollOffsetY = (float) (relativeY / (float) totalSpace);
		scrollOffsetY = Mth.clamp(scrollOffsetY, 0.0F, 1.0F);
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		int totalHeight = contents.getHeight();
		if (hiddenHeight > 0) {
			scrollOffsetY = subtractInputFromScroll(totalHeight, scrollOffsetY, scrollDeltaY);
		} else {
			scrollOffsetY = 0.0f;
		}
		return true;
	}

	@Override
	public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (dragOriginY < 0 || mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();

		double topY = mouseY - dragOriginY;
		moveScrollbarTo(scrollbarMarkerArea, topY);
		return true;
	}
}
