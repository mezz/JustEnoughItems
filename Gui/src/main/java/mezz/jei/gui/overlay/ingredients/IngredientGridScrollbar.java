package mezz.jei.gui.overlay.ingredients;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

public class IngredientGridScrollbar implements IUserInputHandler {
	public static final int SCROLLBAR_WIDTH = 14;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;

	private final IngredientGridWithNavigationController controller;
	private final ScalableDrawable scrollbarMarker;
	private final ScalableDrawable scrollbarBackground;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private double dragOriginY = -1;

	public IngredientGridScrollbar(IngredientGridWithNavigationController controller) {
		this.controller = controller;
		Textures textures = Internal.getTextures();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.scrollbarBackground = textures.getScrollbarBackground();
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
	}

	public void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (this.area.isEmpty()) {
			return;
		}

		requestCursor(guiGraphics, mouseX, mouseY);

		scrollbarBackground.draw(guiGraphics, area);

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();
		scrollbarMarker.draw(guiGraphics, scrollbarMarkerArea);
	}

	private void requestCursor(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (this.dragOriginY >= 0) {
			guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
			return;
		}

		if (this.area.contains(mouseX, mouseY)) {
			if (controller.canScroll()) {
				guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
			} else {
				guiGraphics.requestCursor(CursorTypes.NOT_ALLOWED);
			}
		}
	}

	private ImmutableRect2i calculateScrollbarMarkerArea() {
		int totalSpace = area.height() - 2;
		int scrollMarkerWidth = area.width() - 2;
		int visibleAmount = controller.getVisibleScrollAmount();
		int hiddenAmount = controller.getHiddenScrollAmount();
		int scrollMarkerHeight = totalSpace;
		if (hiddenAmount > 0) {
			scrollMarkerHeight = Math.round(totalSpace * (visibleAmount / (float) (visibleAmount + hiddenAmount)));
			scrollMarkerHeight = Math.max(scrollMarkerHeight, MIN_SCROLL_MARKER_HEIGHT);
		}
		scrollMarkerHeight = Math.min(scrollMarkerHeight, totalSpace);
		int scrollbarMarkerY = Math.round((totalSpace - scrollMarkerHeight) * controller.getScrollOffsetY());
		return new ImmutableRect2i(
			area.getX() + 1,
			area.getY() + 1 + scrollbarMarkerY,
			scrollMarkerWidth,
			scrollMarkerHeight
		);
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, IGuiProperties guiProperties, UserInput input, IInternalKeyMappings keyBindings) {
		if (!input.is(keyBindings.getLeftClick())) {
			return Optional.empty();
		}

		if (!input.isSimulate()) {
			boolean wasDragging = this.dragOriginY >= 0;
			this.dragOriginY = -1;
			if (wasDragging) {
				return Optional.of(this);
			}
			return Optional.empty();
		}

		if (!this.area.contains(input.getMouseX(), input.getMouseY())) {
			return Optional.empty();
		}

		if (!controller.canScroll()) {
			return Optional.empty();
		}

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();
		if (!scrollbarMarkerArea.contains(input.getMouseX(), input.getMouseY())) {
			moveScrollbarCenterTo(scrollbarMarkerArea, input.getMouseY());
			scrollbarMarkerArea = calculateScrollbarMarkerArea();
		}
		this.dragOriginY = input.getMouseY() - scrollbarMarkerArea.y();
		return Optional.of(this);
	}

	@Override
	public Optional<IUserInputHandler> handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey, double dragX, double dragY) {
		if (this.dragOriginY < 0 || mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
			return Optional.empty();
		}

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea();
		double topY = mouseY - this.dragOriginY;
		moveScrollbarTo(scrollbarMarkerArea, topY);
		return Optional.of(this);
	}

	@Override
	public void unfocus() {
		this.dragOriginY = -1;
	}

	private void moveScrollbarCenterTo(ImmutableRect2i scrollbarMarkerArea, double centerY) {
		double topY = centerY - (scrollbarMarkerArea.height() / 2.0);
		moveScrollbarTo(scrollbarMarkerArea, topY);
	}

	private void moveScrollbarTo(ImmutableRect2i scrollbarMarkerArea, double topY) {
		int minY = area.y();
		int maxY = area.y() + area.height() - scrollbarMarkerArea.height();
		double relativeY = topY - minY;
		int totalSpace = maxY - minY;
		if (totalSpace > 0) {
			float scrollOffsetY = (float) (relativeY / (float) totalSpace);
			controller.setScrollOffsetY(scrollOffsetY);
		}
	}
}
