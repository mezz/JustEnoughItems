package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IScrollGridWidget;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.GridScrollMath;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.PlaceableUtil;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;

public class ScrollGridRecipeWidget extends AbstractScrollWidget implements IScrollGridWidget, ISlottedRecipeWidget, IJeiInputHandler {
	private final IDrawable slotBackground;
	private final int columns;
	private final int visibleRows;
	private final int hiddenRows;
	private final List<IRecipeSlotDrawable> slots;

	public static ImmutableSize2i calculateSize(int columns, int visibleRows) {
		IDrawable slotBackground = Internal.getTextures().getSlot();
		return new ImmutableSize2i(
			columns * slotBackground.getWidth() + getScrollBoxScrollbarExtraWidth(),
			visibleRows * slotBackground.getHeight()
		);
	}

	public static ScrollGridRecipeWidget create(List<IRecipeSlotDrawable> slots, int columns, int visibleRows) {
		ImmutableSize2i size = calculateSize(columns, visibleRows);
		ImmutableRect2i area = new ImmutableRect2i(0, 0, size.width(), size.height());
		return new ScrollGridRecipeWidget(area, columns, visibleRows, slots);
	}

	public ScrollGridRecipeWidget(ImmutableRect2i area, int columns, int visibleRows, List<IRecipeSlotDrawable> slots) {
		super(area);
		this.slots = slots;
		this.slotBackground = Internal.getTextures().getSlot();

		this.columns = columns;
		this.visibleRows = visibleRows;
		this.hiddenRows = GridScrollMath.getHiddenRows(slots.size(), columns, visibleRows);
	}

	@Override
	public ScrollGridRecipeWidget setPosition(int xPos, int yPos) {
		this.area = area.setPosition(xPos, yPos);
		return this;
	}

	@Override
	public ScrollGridRecipeWidget setPosition(
		int areaX,
		int areaY,
		int areaWidth,
		int areaHeight,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	) {
		PlaceableUtil.setPosition(
			this,
			areaX,
			areaY,
			areaWidth,
			areaHeight,
			horizontalAlignment,
			verticalAlignment
		);
		return this;
	}

	@Override
	public int getWidth() {
		return area.width();
	}

	@Override
	public int getHeight() {
		return area.height();
	}

	@Override
	protected int getVisibleAmount() {
		if (isSmoothScrolling()) {
			return visibleRows * slotBackground.getHeight();
		}
		return visibleRows;
	}

	@Override
	protected int getHiddenAmount() {
		if (isSmoothScrolling()) {
			return hiddenRows * slotBackground.getHeight();
		}
		return hiddenRows;
	}

	@Override
	protected void drawContents(PoseStack poseStack, double mouseX, double mouseY, float scrollOffsetY) {
		final int totalSlots = slots.size();
		final int scrollPixelOffset = getScrollPixelOffset();

		final int slotWidth = slotBackground.getWidth();
		final int slotHeight = slotBackground.getHeight();
		final int firstRow = scrollPixelOffset / slotHeight;
		final int firstIndex = columns * firstRow;
		final int rowPixelOffset = scrollPixelOffset % slotHeight;
		int renderedRows = visibleRows;
		if (rowPixelOffset > 0) {
			renderedRows++;
		}

		if (rowPixelOffset > 0) {
			mezz.jei.common.gui.ScissorHelper.enableScissor(poseStack,
				contentsArea.x(),
				contentsArea.y(),
				contentsArea.x() + contentsArea.width(),
				contentsArea.y() + contentsArea.height()
			);
		}

		try {
			for (int row = 0; row < renderedRows; row++) {
				for (int column = 0; column < columns; column++) {
					final int slotIndex = firstIndex + (row * columns) + column;
					ImmutableRect2i slotArea = getSlotArea(column, row, rowPixelOffset);
					slotBackground.draw(poseStack, slotArea.x(), slotArea.y());
					if (slotIndex < totalSlots) {
						IRecipeSlotDrawable slot = slots.get(slotIndex);
						setSlotPosition(slot, slotArea);
						boolean hovered = isMouseOverSlot(slot, slotArea, mouseX, mouseY);
						slot.draw(poseStack, hovered);
					}
				}
			}
		} finally {
			if (rowPixelOffset > 0) {
				mezz.jei.common.gui.ScissorHelper.disableScissor();
			}
		}
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		if (!contentsArea.contains(mouseX, mouseY)) {
			return Optional.empty();
		}
		final int firstRow = getFirstRow();
		final int startIndex = firstRow * columns;
		final int rowPixelOffset = getRowPixelOffset();
		int visibleRowCount = visibleRows;
		if (rowPixelOffset > 0) {
			visibleRowCount++;
		}
		final int endIndex = Math.min(startIndex + (visibleRowCount * columns), slots.size());
		for (int i = startIndex; i < endIndex; i++) {
			int visibleIndex = i - startIndex;
			int column = visibleIndex % columns;
			int row = visibleIndex / columns;
			ImmutableRect2i slotArea = getSlotArea(column, row, rowPixelOffset);
			IRecipeSlotDrawable slot = slots.get(i);
			setSlotPosition(slot, slotArea);
			if (isMouseOverSlot(slot, slotArea, mouseX, mouseY)) {
				return Optional.of(new RecipeSlotUnderMouse(slot, getArea().getX(), getArea().getY()));
			}
		}
		return Optional.empty();
	}

	private ImmutableRect2i getSlotArea(int column, int row, int rowPixelOffset) {
		int slotWidth = slotBackground.getWidth();
		int slotHeight = slotBackground.getHeight();
		return new ImmutableRect2i(
			column * slotWidth,
			(row * slotHeight) - rowPixelOffset,
			slotWidth,
			slotHeight
		);
	}

	private static void setSlotPosition(IRecipeSlotDrawable slot, ImmutableRect2i slotArea) {
		slot.setPosition(slotArea.x() + 1, slotArea.y() + 1);
	}

	private boolean isMouseOverSlot(IRecipeSlotDrawable slot, ImmutableRect2i slotArea, double mouseX, double mouseY) {
		return contentsArea.contains(mouseX, mouseY) &&
			(slotArea.contains(mouseX, mouseY) || slot.isMouseOver(mouseX, mouseY));
	}

	@Override
	protected float calculateScrollAmount(double scrollDeltaY) {
		if (isSmoothScrolling()) {
			int hiddenPixels = hiddenRows * slotBackground.getHeight();
			if (hiddenPixels == 0) {
				return 0;
			}
			IClientConfig clientConfig = Internal.getClientConfigs().getClientConfig();
			return (float) (scrollDeltaY * clientConfig.smoothScrollRate().get() / (double) hiddenPixels);
		}
		return (float) (scrollDeltaY / (double) hiddenRows);
	}

	private boolean isSmoothScrolling() {
		return Internal.getClientConfigs()
			.getClientConfig()
			.smoothScrollingEnabled()
			.get();
	}

	private int getScrollPixelOffset() {
		if (isSmoothScrolling()) {
			return GridScrollMath.getSmoothScrollPixelOffset(hiddenRows, slotBackground.getHeight(), getScrollOffsetY());
		}
		return getFirstRow() * slotBackground.getHeight();
	}

	private int getFirstRow() {
		if (isSmoothScrolling()) {
			return GridScrollMath.getFirstRowForSmoothScrollPixelOffset(getScrollPixelOffset(), slotBackground.getHeight());
		}
		return GridScrollMath.getFirstRowForScrollOffset(hiddenRows, getScrollOffsetY());
	}

	private int getRowPixelOffset() {
		return getScrollPixelOffset() % slotBackground.getHeight();
	}
}
