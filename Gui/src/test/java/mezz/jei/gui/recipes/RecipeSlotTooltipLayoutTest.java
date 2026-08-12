package mezz.jei.gui.recipes;

import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeSlotTooltipLayoutTest {
	@Test
	void tooltipBackgroundAbutsRightSideOfSourceSlot() {
		ImmutableRect2i sourceArea = new ImmutableRect2i(100, 50, 18, 18);

		RecipeSlotTooltipLayout.Result result = RecipeSlotTooltipLayout.create(320, 240, sourceArea, 80, 40, 0, 0);

		assertEquals(130, result.x());
		assertEquals(62, result.y());
		assertEquals(new ImmutableRect2i(118, 50, 104, 64), result.area());
	}

	@Test
	void tooltipBackgroundAbutsLeftSideWhenThereIsNotEnoughRoomOnTheRight() {
		ImmutableRect2i sourceArea = new ImmutableRect2i(290, 50, 18, 18);

		RecipeSlotTooltipLayout.Result result = RecipeSlotTooltipLayout.create(320, 240, sourceArea, 80, 40, 0, 0);

		assertEquals(198, result.x());
		assertEquals(62, result.y());
		assertEquals(new ImmutableRect2i(186, 50, 104, 64), result.area());
	}

	@Test
	void tooltipStaysOnScreenNearTheBottom() {
		ImmutableRect2i sourceArea = new ImmutableRect2i(100, 220, 18, 18);

		RecipeSlotTooltipLayout.Result result = RecipeSlotTooltipLayout.create(320, 240, sourceArea, 80, 40, 0, 0);

		assertEquals(130, result.x());
		assertEquals(188, result.y());
		assertEquals(new ImmutableRect2i(118, 176, 104, 64), result.area());
	}
}
