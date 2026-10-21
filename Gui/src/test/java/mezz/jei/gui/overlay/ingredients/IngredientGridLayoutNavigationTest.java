package mezz.jei.gui.overlay.ingredients;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngredientGridLayoutNavigationTest {
	@Test
	void arrowsMoveWithinRowsAndColumns() {
		var slots = slots(9);
		assertEquals(3, IngredientGridLayout.findNextSlot(slots, 3, 4, -1, false));
		assertEquals(5, IngredientGridLayout.findNextSlot(slots, 3, 4, 1, false));
		assertEquals(1, IngredientGridLayout.findNextSlot(slots, 3, 4, -3, true));
		assertEquals(7, IngredientGridLayout.findNextSlot(slots, 3, 4, 3, true));
	}

	@Test
	void excludedCellsDoNotChangeTheColumnOrRow() {
		var slots = slots(9, 4);
		assertEquals(7, IngredientGridLayout.findNextSlot(slots, 3, 1, 3, true));
		assertEquals(1, IngredientGridLayout.findNextSlot(slots, 3, 7, -3, true));
		assertEquals(5, IngredientGridLayout.findNextSlot(slots, 3, 3, 1, false));
		assertEquals(3, IngredientGridLayout.findNextSlot(slots, 3, 5, -1, false));
	}

	@Test
	void partialFinalRowUsesTheNearestAvailableColumn() {
		var slots = slots(8, 6, 7);
		assertEquals(5, IngredientGridLayout.findNextSlot(slots, 4, 3, 4, true));
		assertEquals(1, IngredientGridLayout.findNextSlot(slots, 4, 5, -4, true));
	}

	@Test
	void verticalMovementFallsBackToTheNearestColumnWhenTheLaneIsEmpty() {
		var slots = slots(9, 1, 4, 6, 8);
		assertEquals(3, IngredientGridLayout.findNextSlot(slots, 3, 7, -3, true));
		assertEquals(7, IngredientGridLayout.findNextSlot(slots, 3, 5, 3, true));
	}

	@Test
	void arrowsDoNotWrapAtTheOutsideEdges() {
		var slots = slots(9);
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 3, -1, false));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 5, 1, false));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 1, -3, true));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 7, 3, true));
	}

	@Test
	void tabCrossesRowsAndSkipsEmptySlots() {
		var slots = slots(6, 2, 3);
		assertEquals(4, IngredientGridLayout.findNextSlot(slots, 3, 1, 1, true));
		assertEquals(1, IngredientGridLayout.findNextSlot(slots, 3, 4, -1, true));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 1, 1, false));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 4, -1, false));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 0, -1, true));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 3, 5, 1, true));
	}

	@Test
	void enteringANewPagePreservesTheColumn() {
		var slots = slots(12);
		assertEquals(2, IngredientGridLayout.findNextSlot(slots, 4, -2, 4, true));
		assertEquals(10, IngredientGridLayout.findNextSlot(slots, 4, 14, -4, true));
		assertEquals(0, IngredientGridLayout.findNextSlot(slots, 4, -1, 1, true));
		assertEquals(11, IngredientGridLayout.findNextSlot(slots, 4, 12, -1, true));
	}

	@Test
	void singleColumnOnlyAllowsVerticalMovementOrTab() {
		var slots = slots(3);
		assertEquals(2, IngredientGridLayout.findNextSlot(slots, 1, 1, 1, true));
		assertEquals(0, IngredientGridLayout.findNextSlot(slots, 1, 1, -1, true));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 1, 1, 1, false));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots, 1, 1, -1, false));
	}

	@Test
	void emptyResultsHaveNoNextSlot() {
		assertEquals(-1, IngredientGridLayout.findNextSlot(List.of(), 3, -1, 1, true));
		assertEquals(-1, IngredientGridLayout.findNextSlot(slots(3, 0, 1, 2), 3, -3, 3, true));
	}

	private static List<Boolean> slots(int count, int... empty) {
		var slots = new ArrayList<>(Collections.nCopies(count, true));
		for (int index : empty) {
			slots.set(index, false);
		}
		return slots;
	}
}
