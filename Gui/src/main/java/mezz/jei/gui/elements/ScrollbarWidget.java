package mezz.jei.gui.elements;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.IUserInputHandler;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public final class ScrollbarWidget implements IUserInputHandler {
	private final IScrollbarController controller;
	private final Scrollbar scrollbar = new Scrollbar(ImmutableRect2i.EMPTY);

	public ScrollbarWidget(IScrollbarController controller) {
		this.controller = controller;
	}

	public void updateBounds(ImmutableRect2i area) {
		scrollbar.updateBounds(area);
		if (area.isEmpty()) {
			scrollbar.stopDrag();
		}
	}

	public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (scrollbar.getArea().isEmpty()) {
			return;
		}
		int hiddenAmount = controller.getHiddenScrollAmount();
		if (scrollbar.isDragging()) {
			graphics.requestCursor(CursorTypes.RESIZE_NS);
		} else if (scrollbar.isMouseOver(mouseX, mouseY)) {
			if (hiddenAmount > 0) {
				graphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				graphics.requestCursor(CursorTypes.NOT_ALLOWED);
			}
		}
		scrollbar.draw(graphics, controller.getVisibleScrollAmount(), hiddenAmount, controller.getScrollOffsetY());
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (!input.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}
		if (!input.isSimulate()) {
			boolean dragging = scrollbar.isDragging();
			scrollbar.stopDrag();
			if (dragging) {
				return Optional.of(this);
			}
			return Optional.empty();
		}
		Scrollbar.ScrollResult result = scrollbar.startDrag(
			input.getMouseX(),
			input.getMouseY(),
			controller.getVisibleScrollAmount(),
			controller.getHiddenScrollAmount(),
			controller.getScrollOffsetY()
		);
		return apply(result);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return Optional.empty();
		}
		return apply(scrollbar.dragTo(
			mouseY,
			controller.getVisibleScrollAmount(),
			controller.getHiddenScrollAmount(),
			controller.getScrollOffsetY()
		));
	}

	private Optional<IUserInputHandler> apply(Scrollbar.ScrollResult result) {
		if (!result.handled()) {
			return Optional.empty();
		}
		controller.setScrollOffsetY(result.scrollOffsetY());
		return Optional.of(this);
	}

	@Override
	public void unfocus() {
		scrollbar.stopDrag();
	}
}
