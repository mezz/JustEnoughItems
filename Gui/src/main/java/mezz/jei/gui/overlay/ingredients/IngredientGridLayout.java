package mezz.jei.gui.overlay.ingredients;

import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.gui.ingredients.GuiIngredientProperties;
import mezz.jei.gui.util.AlignmentUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class IngredientGridLayout {
	public static final int INGREDIENT_PADDING = 1;
	public static final int INGREDIENT_WIDTH = GuiIngredientProperties.getWidth(INGREDIENT_PADDING);
	public static final int INGREDIENT_HEIGHT = GuiIngredientProperties.getHeight(INGREDIENT_PADDING);

	private IngredientGridLayout() {

	}

	public static ImmutableSize2i calculateSize(IIngredientGridConfig config, ImmutableRect2i availableArea) {
		final int columns = Math.min(availableArea.getWidth() / INGREDIENT_WIDTH, config.maxColumns().getValue());
		final int rows = Math.min(availableArea.getHeight() / INGREDIENT_HEIGHT, config.maxRows().getValue());
		if (rows < config.getMinRows() || columns < config.getMinColumns()) {
			return ImmutableSize2i.EMPTY;
		}
		return new ImmutableSize2i(
			columns * INGREDIENT_WIDTH,
			rows * INGREDIENT_HEIGHT
		);
	}

	public static ImmutableRect2i calculateBounds(IIngredientGridConfig config, ImmutableRect2i availableArea) {
		ImmutableSize2i size = calculateSize(config, availableArea);
		return AlignmentUtil.align(size, availableArea, config.horizontalAlignment().getValue(), config.verticalAlignment().getValue());
	}

	public static int calculateAvailableSlotCount(
		ImmutableRect2i area,
		Set<ImmutableRect2i> exclusionAreas,
		@Nullable ImmutablePoint2i mouseExclusionPoint
	) {
		int blocked = 0;
		List<SlotLayout> slotLayouts = calculateSlots(area, exclusionAreas, mouseExclusionPoint, 0);
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
		int rowPixelOffset = Math.clamp(smoothScrollRowPixelOffset, 0, INGREDIENT_HEIGHT - 1);
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

	public record SlotLayout(ImmutableRect2i area, boolean blocked) {

	}
}
