package mezz.jei.gui.overlay.ingredients;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.gui.elements.Scrollbar;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class IngredientGridScrollbar implements IUserInputHandler {
	public static final int SCROLLBAR_WIDTH = Scrollbar.WIDTH;

	private final IngredientGridScrollController controller;
	private final Runnable onLayoutChanged;
	private final Scrollbar scrollbar;

	public IngredientGridScrollbar(IngredientGridScrollController controller, Runnable onLayoutChanged) {
		this.controller = controller;
		this.onLayoutChanged = onLayoutChanged;
		this.scrollbar = new Scrollbar(ImmutableRect2i.EMPTY);
	}

	public void updateBounds(ImmutableRect2i area) {
		this.scrollbar.updateBounds(area);
	}

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (this.scrollbar.getArea().isEmpty()) {
			return;
		}

		this.scrollbar.draw(
			guiGraphics,
			this.controller.getVisibleScrollAmount(),
			this.controller.getHiddenScrollAmount(),
			this.controller.getScrollOffsetY()
		);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		if (!input.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}

		if (!input.isSimulate()) {
			boolean wasDragging = this.scrollbar.isDragging();
			this.scrollbar.stopDrag();
			if (wasDragging) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		if (!controller.canScroll()) {
			return Optional.empty();
		}

		Scrollbar.ScrollResult result = this.scrollbar.startDrag(
			input.getMouseX(),
			input.getMouseY(),
			this.controller.getVisibleScrollAmount(),
			this.controller.getHiddenScrollAmount(),
			this.controller.getScrollOffsetY()
		);
		if (!result.handled()) {
			return Optional.empty();
		}
		if (this.controller.setScrollOffsetY(result.scrollOffsetY())) {
			this.onLayoutChanged.run();
		}
		return Optional.of(this);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return Optional.empty();
		}

		Scrollbar.ScrollResult result = this.scrollbar.dragTo(
			mouseY,
			this.controller.getVisibleScrollAmount(),
			this.controller.getHiddenScrollAmount(),
			this.controller.getScrollOffsetY()
		);
		if (!result.handled()) {
			return Optional.empty();
		}
		if (this.controller.setScrollOffsetY(result.scrollOffsetY())) {
			this.onLayoutChanged.run();
		}
		return Optional.of(this);
	}

	@Override
	public void unfocus() {
		this.scrollbar.stopDrag();
	}
}
