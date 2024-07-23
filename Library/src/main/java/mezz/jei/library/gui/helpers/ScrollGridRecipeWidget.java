package mezz.jei.library.gui.helpers;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;

public class ScrollGridRecipeWidget implements ISlottedRecipeWidget, IJeiInputHandler {
	public static final int SCROLLBAR_PADDING = 2;
	public static final int SCROLLBAR_WIDTH = 14;
	private static final int MIN_SCROLL_MARKER_HEIGHT = 14;

	private final DrawableNineSliceTexture scrollbarMarker;
	private final DrawableNineSliceTexture scrollbarBackground;
	private final IDrawable slotBackground;
	private final int columns;
	private final int visibleRows;
	private final int hiddenRows;
	private final List<IRecipeSlotDrawable> slots;
	private final ScreenRectangle area;
	private final ImmutableRect2i scrollArea;
	/**
	 * Position of the mouse on the scroll marker when dragging.
	 */
	private double dragOriginY = -1;
	/**
	 * Amount scrolled in percent, (0 = top, 1 = bottom)
	 */
	private float scrollOffsetY = 0;

	public static ImmutableSize2i calculateSize(IDrawable slotBackground, int columns, int visibleRows) {
		return new ImmutableSize2i(
			columns * slotBackground.getWidth() + SCROLLBAR_PADDING + SCROLLBAR_WIDTH,
			visibleRows * slotBackground.getHeight()
		);
	}

	public ScrollGridRecipeWidget(ScreenRectangle area, int columns, int visibleRows, List<IRecipeSlotDrawable> slots) {
		this.slots = slots;
		Textures textures = Internal.getTextures();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.slotBackground = textures.getSlotDrawable();

		this.columns = columns;
		this.visibleRows = visibleRows;
		this.area = area;
		this.scrollArea = calculateScrollArea(area.width(), area.height());
		int totalRows = MathUtil.divideCeil(slots.size(), columns);
		this.hiddenRows = Math.max(totalRows - visibleRows, 0);
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

	private ImmutableRect2i calculateScrollbarMarkerArea(int hiddenRows) {
		int totalSpace = scrollArea.height() - 2;
		int scrollMarkerWidth = scrollArea.width() - 2;
		int scrollMarkerHeight = Math.round(totalSpace * (visibleRows / (float) (visibleRows + hiddenRows)));
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

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea(hiddenRows);
		scrollbarMarker.draw(guiGraphics, scrollbarMarkerArea);

		final int totalSlots = slots.size();
		final int firstRow = getRowIndexForScroll(hiddenRows, scrollOffsetY);
		final int firstIndex = columns * firstRow;

		final int slotWidth = slotBackground.getWidth();
		final int slotHeight = slotBackground.getHeight();

		for (int row = 0; row < visibleRows; row++) {
			final int y = row * slotHeight;
			for (int column = 0; column < columns; column++) {
				final int x = column * slotWidth;
				final int slotIndex = firstIndex + (row * columns) + column;
				slotBackground.draw(guiGraphics, x, y);
				if (slotIndex < totalSlots) {
					IRecipeSlotDrawable slot = slots.get(slotIndex);
					slot.setPosition(x + 1, y + 1);
					slot.draw(guiGraphics);
				}
			}
		}
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		final int firstRow = getRowIndexForScroll(hiddenRows, scrollOffsetY);
		final int startIndex = firstRow * columns;
		final int endIndex = Math.min(startIndex + (visibleRows * columns), slots.size());
		for (int i = startIndex; i < endIndex; i++) {
			IRecipeSlotDrawable slot = slots.get(i);
			if (slot.isMouseOver(mouseX, mouseY)) {
				return Optional.of(new RecipeSlotUnderMouse(slot, getPosition()));
			}
		}
		return Optional.empty();
	}

	private static int getRowIndexForScroll(int hiddenRows, float scrollOffset) {
		int rowIndex = (int) ((double) (scrollOffset * (float) hiddenRows) + 0.5D);
		return Math.max(rowIndex, 0);
	}

	private static float subtractInputFromScroll(int hiddenRows, float scrollOffset, double scrollDeltaY) {
		float newScrollOffset = scrollOffset - (float) (scrollDeltaY / (double) hiddenRows);
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
			if (hiddenRows == 0) {
				return false;
			}

			if (userInput.isSimulate()) {
				ImmutableRect2i scrollMarkerArea = calculateScrollbarMarkerArea(hiddenRows);
				if (!scrollMarkerArea.contains(mouseX, mouseY)) {
					moveScrollbarCenterTo(scrollMarkerArea, mouseY);
					scrollMarkerArea = calculateScrollbarMarkerArea(hiddenRows);
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
		int totalSlots = slots.size();
		if (hiddenRows > 0) {
			scrollOffsetY = subtractInputFromScroll(totalSlots, scrollOffsetY, scrollDeltaY);
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

		ImmutableRect2i scrollbarMarkerArea = calculateScrollbarMarkerArea(hiddenRows);

		double topY = mouseY - dragOriginY;
		moveScrollbarTo(scrollbarMarkerArea, topY);
		return true;
	}
}
