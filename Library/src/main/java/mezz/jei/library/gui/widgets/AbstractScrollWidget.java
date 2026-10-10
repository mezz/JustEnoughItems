package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ScrollbarVisibility;
import mezz.jei.common.Internal;
import mezz.jei.common.config.NavigationVisibility;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.MouseDrag;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractScrollWidget implements IRecipeWidget, IJeiInputHandler, IInputTarget {
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
	private ScrollbarVisibility scrollbarVisibility = ScrollbarVisibility.DEFAULT;
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
	protected abstract void drawContents(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY, float scrollOffsetY);

	protected float getScrollOffsetY() {
		return scrollOffsetY;
	}

	public AbstractScrollWidget setScrollbarVisibility(ScrollbarVisibility visibility) {
		this.scrollbarVisibility = Objects.requireNonNull(visibility);
		if (!isScrollbarVisible()) {
			scrollbar.stopDrag();
		}
		return this;
	}

	protected final boolean isScrollbarVisible() {
		NavigationVisibility visibility = switch (scrollbarVisibility) {
			case DEFAULT -> Internal.getClientConfigs().getIngredientListConfig().navigationVisibility().get();
			case ENABLED -> NavigationVisibility.ENABLED;
			case AUTO_HIDE -> NavigationVisibility.AUTO_HIDE;
			case DISABLED -> NavigationVisibility.DISABLED;
		};
		return switch (visibility) {
			case ENABLED -> true;
			case AUTO_HIDE -> getHiddenAmount() > 0;
			case DISABLED -> false;
		};
	}

	protected final void resetScroll() {
		scrollOffsetY = 0;
		scrollbar.stopDrag();
	}

	protected ImmutableRect2i getWidgetArea() {
		return area;
	}

	@Override
	public final ScreenRectangle getArea() {
		return getWidgetArea().toScreenRectangle();
	}

	@Override
	public final ScreenPosition getPosition() {
		return getWidgetArea().getScreenPosition();
	}

	@Override
	public ScreenRectangle getScreenRectangle() {
		return getArea();
	}

	@Override
	public final void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
		if (isScrollbarVisible()) {
			this.scrollbar.draw(guiGraphics, getVisibleAmount(), getHiddenAmount(), scrollOffsetY);
		} else {
			this.scrollbar.stopDrag();
		}
		drawContents(guiGraphics, mouseX, mouseY, scrollOffsetY);
	}

	@Override
	public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
		if (!isScrollbarVisible()) {
			scrollbar.stopDrag();
			return false;
		}
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
	public final Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!isScrollbarVisible() || !input.isMouseInput() || !input.is(keys.getLeftClick()) || getHiddenAmount() == 0) {
			return Optional.empty();
		}
		Scrollbar.ScrollResult result = scrollbar.startDrag(input.getMouseX(), input.getMouseY(), getVisibleAmount(), getHiddenAmount(), scrollOffsetY);
		if (!result.handled()) {
			return Optional.empty();
		}
		scrollOffsetY = result.scrollOffsetY();
		return Optional.of(MouseDrag.forWidget(this::handleMouseDragged, scrollbar::stopDrag));
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
		if (!isScrollbarVisible() || getHiddenAmount() == 0) {
			scrollbar.stopDrag();
			return false;
		}
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
