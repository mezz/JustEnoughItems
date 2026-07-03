package mezz.jei.gui.overlay;

import mezz.jei.common.util.ImmutableRect2i;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IngredientListOverlayLayoutTest {
	@Test
	public void rightSideLayoutReservesBottomSearchRow() {
		// Setup: a regular GUI leaves JEI room on the right, and the ingredient grid has a background area.
		TestGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 50, 200, 100);
		ImmutableRect2i contentsArea = new ImmutableRect2i(160, 8, 40, 40);

		// Operation: calculate the overlay layout and the search/config row for contents with room.
		IngredientListOverlayLayout.Layout layout = calculate(
			guiProperties,
			false,
			false,
			false,
			0
		);
		IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas = layout.getSearchAndConfigAreas(
			true,
			contentsArea
		);

		// Assertions: the content area is on the right and reserves room for the bottom search row.
		assertStartsAtGuiRight(layout.displayArea(), guiProperties);
		assertContainedBy(layout.availableContentsArea(), layout.displayArea());
		assertTrue(
			bottom(layout.availableContentsArea()) < bottom(layout.displayArea()),
			"right-side layout should reserve vertical room for the search row"
		);
		assertEquals(Optional.empty(), layout.lookupHistoryArea());
		assertFalse(layout.searchBarCentered());
		assertContainedBy(searchAndConfigAreas.searchArea(), layout.displayArea());
		assertContainedBy(searchAndConfigAreas.configButtonArea(), layout.displayArea());
		assertPositiveArea(searchAndConfigAreas.searchArea());
		assertPositiveArea(searchAndConfigAreas.configButtonArea());
		assertSharedControlRow(searchAndConfigAreas);
	}

	@Test
	public void rightSideLayoutUsesDisplayAreaForSearchWhenContentsHasNoRoom() {
		// Setup: a regular GUI leaves JEI room on the right, but the contents cannot provide a usable width.
		TestGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 50, 200, 100);
		ImmutableRect2i staleContentsArea = new ImmutableRect2i(160, 8, 40, 40);

		// Operation: calculate the search/config row when contents have no usable bounds.
		IngredientListOverlayLayout.Layout layout = calculate(
			guiProperties,
			false,
			false,
			false,
			0
		);

		IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas = layout.getSearchAndConfigAreas(
			false,
			staleContentsArea
		);

		// Assertions: the search row falls back to the display area instead of depending on contents bounds.
		assertContainedBy(searchAndConfigAreas.searchArea(), layout.displayArea());
		assertContainedBy(searchAndConfigAreas.configButtonArea(), layout.displayArea());
		assertPositiveArea(searchAndConfigAreas.searchArea());
		assertPositiveArea(searchAndConfigAreas.configButtonArea());
		assertSharedControlRow(searchAndConfigAreas);
	}

	@Test
	public void centeredSearchBarUsesGuiWidthAndLeavesContentsFullHeight() {
		// Setup: center-search is enabled and there is enough room below the GUI to place the search field.
		TestGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 50, 260, 100);
		ImmutableRect2i contentsArea = new ImmutableRect2i(210, 8, 40, 40);

		// Operation: calculate the centered layout and search/config row.
		IngredientListOverlayLayout.Layout layout = calculate(
			guiProperties,
			true,
			false,
			false,
			0
		);
		IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas = layout.getSearchAndConfigAreas(
			true,
			contentsArea
		);

		// Assertions: centering leaves the right-side contents available and places controls below the GUI.
		assertStartsAtGuiRight(layout.displayArea(), guiProperties);
		assertContainedBy(layout.availableContentsArea(), layout.displayArea());
		assertTrue(layout.searchBarCentered());
		assertPositiveArea(searchAndConfigAreas.searchArea());
		assertPositiveArea(searchAndConfigAreas.configButtonArea());
		assertSharedControlRow(searchAndConfigAreas);
		assertControlRowWithinGuiWidth(searchAndConfigAreas, guiProperties);
		assertTrue(
			searchAndConfigAreas.searchArea().y() >= guiProperties.guiBottom(),
			"centered search controls should be below the GUI"
		);
	}

	@Test
	public void centeredSearchBarFallsBackWhenThereIsNoRoomBelowGui() {
		// Setup: center-search is enabled, but the GUI bottom leaves no full search row below it.
		TestGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 60, 200, 100);
		ImmutableRect2i contentsArea = new ImmutableRect2i(160, 8, 40, 40);

		// Operation: calculate the layout and search/config row.
		IngredientListOverlayLayout.Layout layout = calculate(
			guiProperties,
			true,
			false,
			false,
			0
		);
		IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas = layout.getSearchAndConfigAreas(
			true,
			contentsArea
		);

		// Assertions: the layout falls back to the right-side bottom search row.
		assertFalse(layout.searchBarCentered());
		assertContainedBy(layout.availableContentsArea(), layout.displayArea());
		assertTrue(
			bottom(layout.availableContentsArea()) < bottom(layout.displayArea()),
			"fallback layout should reserve vertical room for the search row"
		);
		assertContainedBy(searchAndConfigAreas.searchArea(), layout.displayArea());
		assertContainedBy(searchAndConfigAreas.configButtonArea(), layout.displayArea());
		assertPositiveArea(searchAndConfigAreas.searchArea());
		assertPositiveArea(searchAndConfigAreas.configButtonArea());
		assertSharedControlRow(searchAndConfigAreas);
	}

	@Test
	public void sideLookupHistoryReservesRowsBetweenContentsAndSearch() {
		// Setup: lookup history is enabled on the side with two configured rows.
		TestGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 50, 200, 100);

		// Operation: calculate the overlay layout.
		IngredientListOverlayLayout.Layout layout = calculate(
			guiProperties,
			false,
			true,
			true,
			2
		);

		// Assertions: the content area gives those rows to lookup history below contents and above search.
		ImmutableRect2i lookupHistoryArea = layout.lookupHistoryArea()
			.orElseThrow(() -> new AssertionError("lookup history area should be reserved"));
		assertContainedBy(layout.availableContentsArea(), layout.displayArea());
		assertContainedBy(lookupHistoryArea, layout.displayArea());
		assertPositiveArea(lookupHistoryArea);
		assertTrue(
			bottom(layout.availableContentsArea()) <= bottom(lookupHistoryArea),
			"side lookup history should be reserved below the main contents area"
		);
	}

	@Test
	public void lookupHistoryDoesNotReserveRowsWhenDisplayedOnTheOtherSide() {
		// Setup: lookup history is enabled, but it is displayed on the opposite overlay side.
		TestGuiProperties guiProperties = new TestGuiProperties(50, 20, 100, 50, 200, 100);

		// Operation: calculate the overlay layout.
		IngredientListOverlayLayout.Layout layout = calculate(
			guiProperties,
			false,
			true,
			false,
			2
		);

		// Assertions: no side lookup-history area is reserved.
		assertContainedBy(layout.availableContentsArea(), layout.displayArea());
		assertEquals(Optional.empty(), layout.lookupHistoryArea());
	}

	private static void assertStartsAtGuiRight(ImmutableRect2i area, TestGuiProperties guiProperties) {
		assertEquals(guiProperties.guiRight(), area.x(), "display area should start at the GUI right edge");
	}

	private static void assertContainedBy(ImmutableRect2i inner, ImmutableRect2i outer) {
		assertTrue(inner.x() >= outer.x(), () -> inner + " should not start left of " + outer);
		assertTrue(inner.y() >= outer.y(), () -> inner + " should not start above " + outer);
		assertTrue(right(inner) <= right(outer), () -> inner + " should not extend right of " + outer);
		assertTrue(bottom(inner) <= bottom(outer), () -> inner + " should not extend below " + outer);
	}

	private static void assertPositiveArea(ImmutableRect2i area) {
		assertTrue(area.width() > 0, () -> area + " should have positive width");
		assertTrue(area.height() > 0, () -> area + " should have positive height");
	}

	private static void assertSharedControlRow(IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas) {
		ImmutableRect2i searchArea = searchAndConfigAreas.searchArea();
		ImmutableRect2i configButtonArea = searchAndConfigAreas.configButtonArea();
		assertEquals(searchArea.y(), configButtonArea.y(), "search field and config button should share a row");
		assertEquals(searchArea.height(), configButtonArea.height(), "search field and config button should have the same height");
		assertTrue(
			configButtonArea.x() >= right(searchArea),
			"config button should be to the right of the search field"
		);
	}

	private static void assertControlRowWithinGuiWidth(
		IngredientListOverlayLayout.SearchAndConfigAreas searchAndConfigAreas,
		TestGuiProperties guiProperties
	) {
		assertTrue(
			searchAndConfigAreas.searchArea().x() >= guiProperties.guiLeft(),
			"centered search row should start within the GUI width"
		);
		assertTrue(
			right(searchAndConfigAreas.configButtonArea()) <= guiProperties.guiRight(),
			"centered search row should end within the GUI width"
		);
	}

	private static int right(ImmutableRect2i area) {
		return area.x() + area.width();
	}

	private static int bottom(ImmutableRect2i area) {
		return area.y() + area.height();
	}

	private static IngredientListOverlayLayout.Layout calculate(
		TestGuiProperties guiProperties,
		boolean centerSearchBarEnabled,
		boolean lookupHistoryEnabled,
		boolean lookupHistoryDisplayedOnThisSide,
		int maxLookupHistoryRows
	) {
		return IngredientListOverlayLayout.calculate(
			guiProperties,
			centerSearchBarEnabled,
			lookupHistoryEnabled,
			lookupHistoryDisplayedOnThisSide,
			maxLookupHistoryRows
		);
	}
}
