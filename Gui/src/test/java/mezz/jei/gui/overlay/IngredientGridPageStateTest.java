package mezz.jei.gui.overlay;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static mezz.jei.gui.overlay.IngredientGridPageState.findIndexOfIngredientElement;
import static mezz.jei.gui.overlay.IngredientGridPageState.getFirstItemIndexForValidPage;
import static mezz.jei.gui.overlay.IngredientGridPageState.getPageCount;
import static mezz.jei.gui.overlay.IngredientGridPageState.getPageNumberForFirstItemIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class IngredientGridPageStateTest {
	private static final int MISSING_ANCHOR = -1;

	private static final IIngredientType<Object> OBJECT_TYPE = () -> Object.class;
	private static final IIngredientType<String> STRING_TYPE = () -> String.class;

	@Test
	public void validAnchorIndexSelectsContainingPage() {
		// Setup: a recipe/uses click remembers an ingredient, then search or bookmarks update the list so
		// that remembered ingredient is still present but now belongs to an earlier page than the stale page start.
		int anchorOnSecondPage = 17;
		int itemCount = 23;
		int pageSize = 10;

		// Operation: use the anchor index as the requested item to render after the list changes.
		int firstItemIndex = getFirstItemIndexForValidPage(anchorOnSecondPage, itemCount, pageSize);

		// Assertions: the grid starts on the page containing the anchor.
		assertEquals(10, firstItemIndex);
	}

	@Test
	public void missingAnchorResetsToFirstPageWhenCurrentPageStillExists() {
		// Setup: the remembered ingredient was removed by filtering or bookmark changes, but enough ingredients
		// remain that the user's previous page would still be renderable.
		int itemCount = 23;
		int pageSize = 10;

		// Operation: a missing anchor is represented by -1, so it has no containing page to preserve.
		int firstItemIndex = getFirstItemIndexForValidPage(MISSING_ANCHOR, itemCount, pageSize);

		// Assertions: missing the anchor resets to the first page instead of preserving an arbitrary old page.
		assertEquals(0, firstItemIndex);
	}

	@Test
	public void anchorIndexResetsToZeroWhenGridHasNoSlots() {
		// Setup: exclusions or a very small screen leave no room for the overlay grid, so neither a clicked
		// anchor nor the current page can be displayed.
		int anchorIndex = 17;
		int itemCount = 23;
		int noSlots = 0;

		// Operation: calculate the page start for a grid with no room.
		int firstItemIndex = getFirstItemIndexForValidPage(anchorIndex, itemCount, noSlots);

		// Assertions: without slot capacity, every anchor falls back to the neutral page start.
		assertEquals(0, firstItemIndex);
	}

	@Test
	public void firstItemIndexClampsToLastPage() {
		// Setup: page navigation previously moved forward, then filtering or list rebuilds reduced the total
		// ingredients so the stored first item points past the available entries.
		int firstItemIndexPastEnd = 40;
		int itemCount = 23;
		int pageSize = 10;

		// Operation: normalize the stored first item before rendering.
		int firstItemIndex = getFirstItemIndexForValidPage(firstItemIndexPastEnd, itemCount, pageSize);

		// Assertions: the closest valid page is the final partial page.
		assertEquals(20, firstItemIndex);
	}

	@Test
	public void requestedPageStartResetsToZeroWhenGridHasNoSlots() {
		// Setup: the ingredient list still has entries, but screen exclusions or window size make the grid
		// temporarily unable to render any slots.
		int currentPageStart = 30;
		int itemCount = 23;
		int noSlots = 0;

		// Operation: normalize the page start for an unrenderable grid.
		int firstItemIndex = getFirstItemIndexForValidPage(currentPageStart, itemCount, noSlots);

		// Assertions: no slot capacity means there is no meaningful non-zero page start.
		assertEquals(0, firstItemIndex);
	}

	@Test
	public void firstItemIndexIsZeroForEmptyList() {
		// Setup: the old page start is non-zero, but search text, edit-mode visibility, or plugin updates
		// removed every ingredient from the source list.
		int currentPageStart = 30;
		int noItems = 0;
		int pageSize = 10;

		// Operation: normalize the page start for an empty list.
		int firstItemIndex = getFirstItemIndexForValidPage(currentPageStart, noItems, pageSize);

		// Assertions: an empty ingredient list always renders from index zero.
		assertEquals(0, firstItemIndex);
	}

	@Test
	public void pageNumberUsesClampedFirstItemIndex() {
		// Setup: the navigation label is refreshed after the list shrank, and the stored first item would
		// point to a page that no longer exists.
		int firstItemIndexPastEnd = 40;
		int pageSize = 10;
		int itemCount = 23;

		// Operation: calculate the displayed page number from the stale first item.
		int pageNumber = getPageNumberForFirstItemIndex(firstItemIndexPastEnd, pageSize, itemCount);

		// Assertions: page number reporting uses the clamped render page.
		assertEquals(2, pageNumber);
	}

	@Test
	public void pageNumberIsZeroForEmptyGrid() {
		// Setup: the overlay is active but has no available slots because the screen layout leaves no grid
		// room, so pagination cannot divide the list into visible pages.
		int currentPageStart = 30;
		int noSlots = 0;
		int itemCount = 23;

		// Operation: calculate the displayed page number for an unrenderable grid.
		int pageNumber = getPageNumberForFirstItemIndex(currentPageStart, noSlots, itemCount);

		// Assertions: page zero is reported as the stable fallback.
		assertEquals(0, pageNumber);
	}

	@Test
	public void pageCountIsOneForEmptyGrid() {
		// Setup: ingredients exist, but the current screen size or exclusion areas leave the grid with no
		// renderable slots.
		int itemCount = 23;
		int noSlots = 0;

		// Operation: calculate the page count for a grid with no capacity.
		int pageCount = getPageCount(itemCount, noSlots);

		// Assertions: navigation still exposes a single logical page instead of zero pages.
		assertEquals(1, pageCount);
	}

	@Test
	public void pageCountIsAtLeastOne() {
		// Setup: filtering, edit-mode hiding, or an ingredient reload has removed every ingredient from a grid
		// that can normally render items.
		int noItems = 0;
		int pageSize = 10;

		// Operation: calculate the page count for an empty ingredient list.
		int pageCount = getPageCount(noItems, pageSize);

		// Assertions: the navigation model keeps one page as its minimum.
		assertEquals(1, pageCount);
	}

	@Test
	public void explicitPageNavigationClearsTheClickedPageAnchor() {
		// Setup: the user clicked an ingredient to open recipes, then manually paged away before the source
		// list changed again. The old clicked ingredient is still in the source list, but it should no longer
		// control future relayouts after explicit page navigation.
		IngredientGridPageState pageState = new IngredientGridPageState();
		IElement<?> clickedAnchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()));
		List<IElement<?>> elements = List.of(clickedAnchor);
		pageState.setPageAnchorElement(clickedAnchor);

		// Operation: explicit page navigation renders a page without using the clicked ingredient as an anchor.
		pageState.updateForPageNavigation(1, elements.size(), 1);
		IElement<?> pageAnchor = pageState.getPageAnchorElement(elements);

		// Assertions: the next relayout will fall back to the first visible element on the navigated-to page,
		// instead of exposing the stale clicked anchor from a previous recipe lookup.
		assertNull(pageAnchor);
	}

	@Test
	public void getPageAnchorElementReturnsRememberedAnchorWhenStillPresent() {
		// Setup: the user clicked an ingredient to open recipes, and the source list still contains that exact
		// element when the overlay needs an anchor for the next relayout.
		IngredientGridPageState pageState = new IngredientGridPageState();
		IElement<?> clickedAnchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()));
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object())),
			clickedAnchor
		);
		pageState.setPageAnchorElement(clickedAnchor);

		// Operation: ask the page state for its explicit anchor.
		IElement<?> pageAnchor = pageState.getPageAnchorElement(elements);

		// Assertions: a valid remembered anchor is returned instead of forcing the caller to use a fallback.
		assertSame(clickedAnchor, pageAnchor);
	}

	@Test
	public void getPageAnchorElementReturnsRememberedAnchorWhenMatchingElementWasRebuilt() {
		// Setup: the list was rebuilt with a new element wrapper, but the remembered clicked ingredient still
		// matches an entry by ingredient identity.
		IngredientGridPageState pageState = new IngredientGridPageState();
		Object ingredient = new Object();
		IElement<?> clickedAnchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, ingredient));
		List<IElement<?>> rebuiltElements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, ingredient))
		);
		pageState.setPageAnchorElement(clickedAnchor);

		// Operation: ask the page state for its explicit anchor after the source list changed.
		IElement<?> pageAnchor = pageState.getPageAnchorElement(rebuiltElements);

		// Assertions: the original remembered anchor is still usable because page-state lookup can locate an
		// equivalent entry in the current list.
		assertSame(clickedAnchor, pageAnchor);
	}

	@Test
	public void getPageAnchorElementClearsStaleAnchor() {
		// Setup: the remembered clicked ingredient is no longer present after filtering or bookmark changes.
		IngredientGridPageState pageState = new IngredientGridPageState();
		IElement<?> staleAnchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()));
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()))
		);
		pageState.setPageAnchorElement(staleAnchor);

		// Operation: ask for the anchor twice after it has gone stale.
		IElement<?> firstLookup = pageState.getPageAnchorElement(elements);
		IElement<?> secondLookup = pageState.getPageAnchorElement(elements);

		// Assertions: the stale anchor is not returned, and it is cleared so the caller owns fallback behavior.
		assertNull(firstLookup);
		assertNull(secondLookup);
	}

	@Test
	public void anchorMatchesTheSameElementInstance() {
		// Setup: the visible first element or clicked ingredient is still the same element object after nearby
		// entries were inserted or removed.
		IElement<?> anchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()));
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object())),
			anchor,
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()))
		);

		// Operation: find where the remembered element appears in the current ingredient list.
		int index = findIndexOfIngredientElement(anchor, elements);

		// Assertions: object identity is the strongest match and preserves the anchor index.
		assertEquals(1, index);
	}

	@Test
	public void anchorMatchesTheSameTypedIngredientInstance() {
		// Setup: the list was rebuilt with new element wrappers, but one rebuilt entry still points to the
		// same typed ingredient object that was remembered.
		ITypedIngredient<Object> typedIngredient = new TestTypedIngredient<>(OBJECT_TYPE, new Object());
		IElement<?> anchor = new IngredientElement<>(typedIngredient);
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object())),
			new IngredientElement<>(typedIngredient)
		);

		// Operation: find the current list entry that represents the remembered ingredient.
		int index = findIndexOfIngredientElement(anchor, elements);

		// Assertions: the anchor survives wrapper replacement when the typed ingredient is unchanged.
		assertEquals(1, index);
	}

	@Test
	public void anchorMatchesTheSameIngredientInstance() {
		// Setup: a source-list rebuild recreated both element and typed-ingredient wrappers, but reused the
		// same underlying ingredient object.
		Object ingredient = new Object();
		IElement<?> anchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, ingredient));
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object())),
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, ingredient))
		);

		// Operation: find the equivalent entry in the current list.
		int index = findIndexOfIngredientElement(anchor, elements);

		// Assertions: matching by ingredient identity keeps the same logical anchor visible.
		assertEquals(1, index);
	}

	@Test
	public void anchorDoesNotMatchADifferentIngredientType() {
		// Setup: a candidate entry was derived from the same value but registered under another ingredient
		// type, such as an item-like value versus a string/debug ingredient.
		Object ingredient = new Object();
		IElement<?> anchor = new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, ingredient));
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(STRING_TYPE, ingredient.toString()))
		);

		// Operation: search for the anchor across the current list.
		int index = findIndexOfIngredientElement(anchor, elements);

		// Assertions: anchors do not cross ingredient type boundaries.
		assertEquals(MISSING_ANCHOR, index);
	}

	@Test
	public void nullAnchorIsMissing() {
		// Setup: the layout is updating before any visible element has been remembered or after the previous
		// anchor was explicitly cleared.
		List<IElement<?>> elements = List.of(
			new IngredientElement<>(new TestTypedIngredient<>(OBJECT_TYPE, new Object()))
		);

		// Operation: search for a missing anchor value.
		int index = findIndexOfIngredientElement(null, elements);

		// Assertions: null anchors are handled as a normal missing-anchor case.
		assertEquals(MISSING_ANCHOR, index);
	}

	private record TestTypedIngredient<T>(IIngredientType<T> type, T ingredient) implements ITypedIngredient<T> {
		@Override
		public IIngredientType<T> getType() {
			return type;
		}

		@Override
		public T getIngredient() {
			return ingredient;
		}
	}

}
