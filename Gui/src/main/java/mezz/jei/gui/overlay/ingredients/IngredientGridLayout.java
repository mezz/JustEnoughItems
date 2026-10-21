package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.util.AlignmentUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class IngredientGridLayout {
	public static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

	private IngredientGridLayout() {

	}

	public static ImmutableSize2i calculateSize(
		IIngredientGridConfig config,
		ImmutableRect2i availableArea,
		boolean allowPartialRows
	) {
		final int columns = Math.min(availableArea.getWidth() / INGREDIENT_WIDTH, config.maxColumns().get());
		final int rows = Math.min(availableArea.getHeight() / INGREDIENT_HEIGHT, config.maxRows().get());
		if (rows < config.getMinRows() || columns < config.getMinColumns()) {
			return ImmutableSize2i.EMPTY;
		}
		int height = rows * INGREDIENT_HEIGHT;
		if (allowPartialRows && rows < config.maxRows().get()) {
			height = availableArea.height();
		}
		return new ImmutableSize2i(
			columns * INGREDIENT_WIDTH,
			height
		);
	}

	public static ImmutableRect2i calculateBounds(
		IIngredientGridConfig config,
		ImmutableRect2i availableArea,
		boolean allowPartialRows
	) {
		ImmutableSize2i size = calculateSize(config, availableArea, allowPartialRows);
		return AlignmentUtil.align(size, availableArea, config.horizontalAlignment().get(), config.verticalAlignment().get());
	}

	public static int calculateAvailableSlotCount(
		ImmutableRect2i area,
		Set<ImmutableRect2i> exclusionAreas
	) {
		int blocked = 0;
		List<SlotLayout> slotLayouts = calculateSlots(area, exclusionAreas, null, 0);
		for (SlotLayout slotLayout : slotLayouts) {
			if (slotLayout.blocked()) {
				blocked++;
			}
		}
		return slotLayouts.size() - blocked;
	}

	public static List<SlotLayout> calculateSlots(
		ImmutableRect2i area,
		Set<ImmutableRect2i> exclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint,
		int smoothScrollRowPixelOffset
	) {
		List<SlotLayout> slotLayouts = new ArrayList<>();
		int rowPixelOffset = net.minecraft.util.Mth.clamp(smoothScrollRowPixelOffset, 0, INGREDIENT_HEIGHT - 1);
		for (int y = area.getY() - rowPixelOffset; y < area.getY() + area.getHeight(); y += INGREDIENT_HEIGHT) {
			for (int x = area.getX(); x < area.getX() + area.getWidth(); x += INGREDIENT_WIDTH) {
				ImmutableRect2i slotArea = new ImmutableRect2i(x, y, INGREDIENT_WIDTH, INGREDIENT_HEIGHT);
				slotLayouts.add(new SlotLayout(
					slotArea,
					isSlotBlocked(slotArea, exclusionAreas, mouseExclusionPoint)
				));
			}
		}
		return slotLayouts;
	}

	private static boolean isSlotBlocked(
		ImmutableRect2i stackArea,
		Set<ImmutableRect2i> exclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		return MathUtil.intersects(exclusionAreas, stackArea.expandBy(2)) ||
			(mouseExclusionPoint != null && stackArea.contains(mouseExclusionPoint));
	}

	/** Steps through slots by one cell or one row, returning -1 when no occupied slot remains. */
	static int findNextSlot(List<Boolean> occupied, int columns, int current, int step, boolean crossRows) {
		for (int next = current + step; next >= 0 && next < occupied.size(); next += step) {
			if (!crossRows && next / columns != current / columns) {
				return -1;
			}
			if (occupied.get(next)) {
				return next;
			}
		}
		// If the column ends, use the nearest populated column in the next available row.
		if (Math.abs(step) > 1) {
			int column = Math.floorMod(current, columns);
			int firstRow = Math.floorDiv(current, columns) + Integer.signum(step);
			for (int row = firstRow * columns; row >= 0 && row < occupied.size(); row += step) {
				for (int offset = 1; offset < columns; offset++) {
					int left = row + column - offset;
					int right = row + column + offset;
					if (left >= row && left < occupied.size() && occupied.get(left)) {
						return left;
					}
					if (right < row + columns && right < occupied.size() && occupied.get(right)) {
						return right;
					}
				}
			}
		}
		return -1;
	}

	public record SlotLayout(ImmutableRect2i area, boolean blocked) {

	}
}
