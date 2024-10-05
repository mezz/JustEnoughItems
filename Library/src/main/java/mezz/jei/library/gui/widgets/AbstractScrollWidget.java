package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

public abstract class AbstractScrollWidget implements IRecipeWidget, IJeiInputHandler {
	private static final int SCROLLBAR_PADDING = 2;
	private static final int SCROLLBAR_WIDTH = 14;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;

	public static int getScrollBoxScrollbarExtraWidth() {
		return SCROLLBAR_WIDTH + SCROLLBAR_PADDING;
	}

	protected static ImmutableRect2i calculateScrollArea(int width, int height) {
		return new ImmutableRect2i(
			width - SCROLLBAR_WIDTH,
			0,
			SCROLLBAR_WIDTH,
			height
		);
	}

	protected ImmutableRect2i area;
	protected final ImmutableRect2i contentsArea;

	private final ImmutableRect2i scrollArea;
	private final DrawableNineSliceTexture scrollbarMarker;
	private final DrawableNineSliceTexture scrollbarBackground;
	/**
	 * Position of the mouse on the scroll marker when dragging.
	 */
	private double dragOriginY = -1;
	/**
	 * Amount scrolled in percent, (0 = top, 1 = bottom)
	 */
	private float scrollOffsetY = 0;

	public AbstractScrollWidget(ImmutableRect2i area) {
		this.area = area;
		this.scrollArea = calculateScrollArea(area.width(), area.height());
		Textures textures = Internal.getTextures();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.contentsArea = new ImmutableRect2i(
			0,
			0,
			area.width() - getScrollBoxScrollbarExtraWidth(),
			area.height()
		);
	}

	protected ImmutableRect2i calculateScrollbarMarkerArea() {
		int totalSpace = scrollArea.height() - 2;
		int scrollMarkerWidth = scrollArea.width() - 2;
		int scrollMarkerHeight = Math.round(totalSpace * (getVisibleAmount() / (float) (getVisibleAmount() + getHiddenAmount())));
		scrollMarkerHeight = Math.max(scrollMarkerHeight, MIN_SCROLL_MARKER_HEIGHT);
		int scrollbarMarkerY = Math.round((totalSpace - scrollMarkerHeight) * scrollOffsetY);
		return new ImmutableRect2i(
			scrollArea.getX() + 1,
			scrollArea.getY() + 1 + scrollbarMarkerY,
			scrollMarkerWidth,
			scrollMarkerHeight
		);
	}

	protected abstract int getVisibleAmount();
	protected abstract int getHiddenAmount();
	protected abstract void drawContents(GuiGraphics guiGraphics, double mouseX, double mouseY, float scrollOffsetY);

	protected float getScrollOffsetY() {
		return scrollOffsetY;
	}

	@Override
	public final ScreenRectangle getArea() {
		return area.toScreenRectangle();
	}

	@Override
	public final ScreenPosition getPosition() {
		return area.getScreenPosition();
	}

	@Override
	public final void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		scrollbarBackground.draw(guiGraphics, scrollArea);

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();
		scrollbarMarker.draw(guiGraphics, scrollbarMarkerArea);

		drawContents(guiGraphics, mouseX, mouseY, scrollOffsetY);
	}

	@Override
	public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
		if (!userInput.is(Internal.getKeyMappings().getLeftClick())) {
			return false;
		}
		if (!userInput.isSimulate()) {
			dragOriginY = -1;
		}

		if (scrollArea.contains(mouseX, mouseY)) {
			if (getHiddenAmount() == 0) {
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

	@Override
	public final boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
		if (getHiddenAmount() > 0) {
			scrollOffsetY -= calculateScrollAmount(scrollDeltaY);
			scrollOffsetY = Mth.clamp(scrollOffsetY, 0.0F, 1.0F);
		} else {
			scrollOffsetY = 0.0f;
		}
		return true;
	}

	@Override
	public final boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (dragOriginY < 0 || mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();

		double topY = mouseY - dragOriginY;
		moveScrollbarTo(scrollbarMarkerArea, topY);
		return true;
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

	protected abstract float calculateScrollAmount(double scrollDeltaY);
}
