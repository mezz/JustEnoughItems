package mezz.jei.library.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;
import java.util.Optional;

public class ScrollGridRecipeWidget extends AbstractScrollWidget implements ISlottedRecipeWidget, IJeiInputHandler {
	private final IDrawable slotBackground;
	private final int columns;
	private final int visibleRows;
	private final int hiddenRows;
	private final List<IRecipeSlotDrawable> slots;

	public static ImmutableSize2i calculateSize(IDrawable slotBackground, int columns, int visibleRows) {
		return new ImmutableSize2i(
			columns * slotBackground.getWidth() + getScrollBoxScrollbarExtraWidth(),
			visibleRows * slotBackground.getHeight()
		);
	}

	public ScrollGridRecipeWidget(Rect2i area, int columns, int visibleRows, List<IRecipeSlotDrawable> slots) {
		super(area);
		this.slots = slots;
		Textures textures = Internal.getTextures();
		this.slotBackground = textures.getSlotDrawable();

		this.columns = columns;
		this.visibleRows = visibleRows;
		int totalRows = MathUtil.divideCeil(slots.size(), columns);
		this.hiddenRows = Math.max(totalRows - visibleRows, 0);
	}

	@Override
	protected int getVisibleAmount() {
		return visibleRows;
	}

	@Override
	protected int getHiddenAmount() {
		return hiddenRows;
	}

	@Override
	protected void drawContents(PoseStack poseStack, double mouseX, double mouseY, float scrollOffsetY) {
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
				slotBackground.draw(poseStack, x, y);
				if (slotIndex < totalSlots) {
					IRecipeSlotDrawable slot = slots.get(slotIndex);
					slot.setPosition(x + 1, y + 1);
					slot.draw(poseStack, false);
				}
			}
		}
	}

	@Override
	public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
		final int firstRow = getRowIndexForScroll(hiddenRows, getScrollOffsetY());
		final int startIndex = firstRow * columns;
		final int endIndex = Math.min(startIndex + (visibleRows * columns), slots.size());
		for (int i = startIndex; i < endIndex; i++) {
			IRecipeSlotDrawable slot = slots.get(i);
			if (slot.isMouseOver(mouseX, mouseY)) {
				Rect2i area = getArea();
				return Optional.of(new RecipeSlotUnderMouse(slot, area.getX(), area.getY()));
			}
		}
		return Optional.empty();
	}

	private int getRowIndexForScroll(int hiddenRows, float scrollOffset) {
		int rowIndex = (int) ((double) (scrollOffset * (float) hiddenRows) + 0.5D);
		return Math.max(rowIndex, 0);
	}

	@Override
	protected float calculateScrollAmount(double scrollDeltaY) {
		int hiddenRows = getHiddenAmount();
		return (float) (scrollDeltaY / (double) hiddenRows);
	}
}
