package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

public abstract class AbstractScrollWidget implements IRecipeWidget, IJeiInputHandler {
	private static final int SCROLLBAR_PADDING = 2;

	public static int getScrollBoxScrollbarExtraWidth() {
		return Scrollbar.WIDTH + SCROLLBAR_PADDING;
	}

	protected static ImmutableRect2i calculateScrollArea(int width, int height) {
		return new ImmutableRect2i(
			width - Scrollbar.WIDTH,
			0,
			Scrollbar.WIDTH,
			height
		);
	}

	protected ImmutableRect2i area;
	protected final ImmutableRect2i contentsArea;

	private final Scrollbar scrollbar;
	/**
	 * Amount scrolled in percent, (0 = top, 1 = bottom)
	 */
	private float scrollOffsetY = 0;

	public AbstractScrollWidget(ImmutableRect2i area) {
		this.area = area;
		this.scrollbar = new Scrollbar(calculateScrollArea(area.width(), area.height()));
		this.contentsArea = new ImmutableRect2i(
			0,
			0,
			area.width() - getScrollBoxScrollbarExtraWidth(),
			area.height()
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
		this.scrollbar.draw(guiGraphics, getVisibleAmount(), getHiddenAmount(), scrollOffsetY);
		drawContents(guiGraphics, mouseX, mouseY, scrollOffsetY);
	}

	@Override
	public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
		if (!userInput.is(Internal.getKeyMappings().getLeftClick())) {
			return false;
		}
		if (!userInput.isSimulate()) {
			this.scrollbar.stopDrag();
		}

		if (this.scrollbar.isMouseOver(mouseX, mouseY)) {
			if (getHiddenAmount() == 0) {
				return false;
			}

			if (userInput.isSimulate()) {
				Scrollbar.ScrollResult result = this.scrollbar.startDrag(
					mouseX,
					mouseY,
					getVisibleAmount(),
					getHiddenAmount(),
					this.scrollOffsetY
				);
				this.scrollOffsetY = result.scrollOffsetY();
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
		if (mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		Scrollbar.ScrollResult result = this.scrollbar.dragTo(
			mouseY,
			getVisibleAmount(),
			getHiddenAmount(),
			this.scrollOffsetY
		);
		this.scrollOffsetY = result.scrollOffsetY();
		return result.handled();
	}

	protected abstract float calculateScrollAmount(double scrollDeltaY);
}
